/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.orchestrator.services;

import io.cloudslang.engine.node.services.WorkerLockService;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.orchestrator.entities.Message;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

import static ch.lambdaj.Lambda.filter;

/**
 * Date: 12/1/13
 *
 * @author
 */
public final class OrchestratorDispatcherServiceImpl implements OrchestratorDispatcherService {
    private final Logger logger = Logger.getLogger(getClass());

    private static int dbConnectionRetries = 5;
    private static int dispatchBulkRetries = 3;

    @Autowired
    private QueueDispatcherService queueDispatcher;

    @Autowired
    private WorkerNodeService workerNodeService;

    @Autowired
    private WorkerLockService workerLockService;

    @Autowired
    private QueueDispatcherHelperService dispatcherHelperService;

    @Override
    @Transactional
    public void dispatch(final List<? extends Serializable> messages, final String bulkNumber, String wrv, final String workerUuid) {
        try {
            //lock to synchronize with the recovery job
            workerLockService.lock(workerUuid);
            Validate.notNull(messages, "Messages list is null");

            String currentBulkNumber = workerNodeService.readByUUID(workerUuid).getBulkNumber();
            //can not be null at this point
            String currentWRV = workerNodeService.readByUUID(workerUuid).getWorkerRecoveryVersion();

            //This is done in order to make sure that if we do retries in worker we won't insert same bulk twice
            if (currentBulkNumber != null && currentBulkNumber.equals(bulkNumber)) {
                logger.warn("Orchestrator got messages bulk with same bulk number: " + bulkNumber + " This bulk was inserted to DB before. Discarding...");
            }
            //This is done in order to make sure that we are not getting messages from worker that was already recovered and does not know about it yet
            else if (!currentWRV.equals(wrv)) {
                logger.warn("Orchestrator got messages from worker: " + workerUuid + " with wrong WRV:" + wrv + " Current WRV is: " + currentWRV + ". Discarding...");
            }
            else {
                dispatchBulk(messages, bulkNumber, workerUuid);
            }
        } catch (Exception ex) {
            logger.error("Failed to dispatch bulk of messages: ", ex);
            throw ex;
        }
    }

    private void dispatchBulk(List<? extends Serializable> messages, String bulkNumber, String workerUuid){
        try {
            for(int i=0; i < dispatchBulkRetries; i++){
                try{
                    //Dispatch list of messages in NEW transaction!!!
                    dispatcherHelperService.dispatchBulk(messages, bulkNumber, workerUuid);

                    break;  //If the insert of bulk worked - stop retries
                }
                catch (Exception bulkException){
                    //If it is the third loop and we still got exception - throw exception
                    if(i == (dispatchBulkRetries - 1) ){
                        logger.error("Failed to dispatch bulk of messages to the queue for 3 times, going to check DB connection! ", bulkException);
                        throw bulkException;
                    }
                    //Sleep one second
                    try{Thread.sleep(1000);} catch (InterruptedException ie){/*ignore*/}
                }
            }

        }
        catch(Exception exc){
            //If not connection problem - try one by one
            if(isDbConnectionOk()){
                logger.error("DB connection is ok, going to dispatch the bulk of messages one by one!");
                dispatchOneByOne(messages);
            }
            else {
                logger.error("No DB connection! Throwing exception to the worker!", exc);
                throw exc; //If it is DB connection failure we want the exception to be thrown and to get to the worker in order to keep the retries/recovery mechanism
            }
        }
    }

    private boolean isDbConnectionOk(){
        boolean result;
        //This method is just checking connection to db for 5 times in separate transaction
        for(int i=0; i < dbConnectionRetries; i++) {
            try {
                result = dispatcherHelperService.isDbConnectionOk();
                if(result){
                    try { Thread.sleep(1000); } catch (InterruptedException e) { /*ignore*/ }
                }
                else {
                    return false;
                }
            }
            catch (Exception ex){
                return false;
            }
        }
        return true;
    }

    private void dispatchOneByOne(List<? extends Serializable> messages) {
        for (Serializable msg : messages) {
            try {
                //We do not update bulk number in case of single inserts to prevent data loss.
                //If worker will do retry with the same bulk then the duplications will be prevented by UNIQUE CONSTRAINT on queues table.
                dispatcherHelperService.dispatch(msg);
            } catch (Exception exp) {
                try {
                    ((Message) msg).setExceptionMessage(exp.getMessage());
                    queueDispatcher.terminateCorruptedMessage((Message) msg);
                }
                catch(Exception exception){
                    logger.error("Failed to terminate corrupted message: ", exception);
                }
            }
        }
    }
}
