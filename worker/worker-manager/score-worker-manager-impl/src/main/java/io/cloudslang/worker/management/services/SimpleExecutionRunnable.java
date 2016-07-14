/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.worker.management.services;

import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.services.QueueStateIdGeneratorService;
import io.cloudslang.orchestrator.entities.SplitMessage;
import io.cloudslang.score.facade.TempConstants;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.facade.execution.ExecutionStatus;
import io.cloudslang.worker.execution.services.ExecutionService;
import io.cloudslang.worker.management.WorkerConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 19/12/12
 */
public class SimpleExecutionRunnable implements Runnable {

    private final Logger logger = Logger.getLogger(this.getClass());

    private ExecutionService executionService;

    private OutboundBuffer outBuffer;

    private InBuffer inBuffer;

    private ExecutionMessageConverter converter;

    private EndExecutionCallback endExecutionCallback;

    private ExecutionMessage executionMessage;

    private QueueStateIdGeneratorService queueStateIdGeneratorService;

    private String workerUUID;

    private WorkerConfigurationService workerConfigurationService;

    private boolean isRecoveryDisabled; //System property - whether the executions are recoverable in case of restart/failure.

    private WorkerManager workerManager;

    public SimpleExecutionRunnable(ExecutionService executionService,
                                   OutboundBuffer outBuffer,
                                   InBuffer inBuffer,
                                   ExecutionMessageConverter converter,
                                   EndExecutionCallback endExecutionCallback,
                                   QueueStateIdGeneratorService queueStateIdGeneratorService,
                                   String workerUUID,
                                   WorkerConfigurationService workerConfigurationService,
                                   WorkerManager workerManager
    ) {
        this.executionService = executionService;
        this.outBuffer = outBuffer;
        this.inBuffer = inBuffer;
        this.converter = converter;
        this.endExecutionCallback = endExecutionCallback;
        this.queueStateIdGeneratorService = queueStateIdGeneratorService;
        this.workerUUID = workerUUID;
        this.workerConfigurationService = workerConfigurationService;
        this.workerManager = workerManager;
        this.isRecoveryDisabled = Boolean.getBoolean("is.recovery.disabled");
    }

    public ExecutionMessage getExecutionMessage() {
        return executionMessage;
    }

    public void setExecutionMessage(ExecutionMessage executionMessage) {
        this.executionMessage = executionMessage;
    }

    @Override
    public void run() {
        String executionId = executionMessage.getMsgId();

        //We are renaming the thread for logging/monitoring purposes
        String origThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(origThreadName + "_" + executionId);
        Execution execution = null;
        try {
            //If we got here because of te shortcut we have the object
            if(executionMessage.getExecutionObject() != null){
                execution = executionMessage.getExecutionObject();

            }
            //If we got here form DB - we need to extract the object from bytes
            else {
                execution = converter.extractExecution(executionMessage.getPayload());
            }

            String branchId = execution.getSystemContext().getBranchId();
            if(logger.isDebugEnabled()){
                logger.debug("Worker starts to work on execution: " + executionId + " branch: " + branchId);
            }


            //Check which logic to trigger - regular execution or split
            if (executionService.isSplitStep(execution)) {
                executeSplitStep(execution);
            } else {
                executeRegularStep(execution);
            }
        }
        catch (InterruptedException interruptedException){

            // not old thread and interrupted by cancel
            boolean oldThread = !workerManager.isFromCurrentThreadPool(Thread.currentThread().getName());
            if(!oldThread && isExecutionCancelled(execution)){
                if (logger.isDebugEnabled())  logger.debug("Execution is interrupted...");
            }else {
                logger.error("Execution thread is interrupted!!! Exiting...", interruptedException);
            }
        }
        catch (Exception ex) {
            logger.error("Error during execution!!!", ex);
            //set status FAILED
            executionMessage.setStatus(ExecStatus.FAILED);
            executionMessage.incMsgSeqId();    //new status must be with incremented msg_seq_id - otherwise will be recovered and we will get duplications
            //send only one execution message back - the new one was not created because of error
            try {
                if(executionMessage.getPayload() == null){
                    executionMessage.setPayload(converter.createPayload(execution)); //this is done since we could get here from InBuffer shortcut - so no payload... and for FAILED message we need to set the payload
                }
                outBuffer.put(executionMessage);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", ex);
            }
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("Worker has finished to work on execution: " + executionId);
            }
            Long executionIdL = null;
            if (!StringUtils.isEmpty(executionId)) {
                executionIdL = Long.valueOf(executionId);
            }
            endExecutionCallback.endExecution(executionIdL);
            //Rename the thread back
            Thread.currentThread().setName(origThreadName);
        }
    }

    private void executeRegularStep(Execution execution) throws InterruptedException {
        Execution nextStepExecution;
        Long startTime = System.currentTimeMillis();

        do {
            //Actually execute the step and get the execution object of the next step
            nextStepExecution = executionService.execute(execution);
        }
        while (!shouldStop(nextStepExecution, startTime));
    }

    private boolean shouldStop(Execution nextStepExecution, Long startTime) {
        //We should stop if
        //1. Thread was interrupted
        //2. execution was paused
        //3. we should stop and go to queue
        //4. The execution is terminating
        //5. The nextStepExecution is a splitStep
        //6. Running too long

        //The order is important!!!
        return isOldThread() ||
                isExecutionCancelled(nextStepExecution) ||
                isExecutionPaused(nextStepExecution) ||
                isExecutionTerminating(nextStepExecution) ||
                isSplitStep(nextStepExecution) ||
                shouldChangeWorkerGroup(nextStepExecution) ||
                isPersistStep(nextStepExecution) ||
                isRecoveryCheckpoint(nextStepExecution) ||
                isRunningTooLong(startTime, nextStepExecution);
    }

    //If execution was paused it sends the current step with status FINISHED and that is all...
    private boolean isExecutionPaused(Execution nextStepExecution) {
        //If execution was paused
        if(nextStepExecution == null){
            //set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();
            executionMessage.setPayload(null);
            //execution was paused - send only the FINISHED message!
            try {
                outBuffer.put(executionMessage);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", e);
            }
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isRecoveryCheckpoint(Execution nextStepExecution) {
        //Here we check if we need to go to queue to persist - we can do it with shortcut to InBuffer!!!!!!!!
        if (!isRecoveryDisabled && nextStepExecution.getSystemContext().containsKey(TempConstants.IS_RECOVERY_CHECKPOINT)) {
            //clean key
            nextStepExecution.getSystemContext().remove(TempConstants.IS_RECOVERY_CHECKPOINT);

            //set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();
            executionMessage.setPayload(null);


            ExecutionMessage inProgressMessage = createInProgressExecutionMessage(nextStepExecution);
            ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage, inProgressMessage}; //for the outBuffer

            ExecutionMessage inProgressMessageForInBuffer = (ExecutionMessage) inProgressMessage.clone();
            inProgressMessageForInBuffer.setPayload(null); //we do not need the payload for the inBuffer shortcut

            try {
                //The order is important!!!!!
                outBuffer.put(executionMessagesToSend);
                inBuffer.addExecutionMessage(inProgressMessageForInBuffer);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", e);
                return true; //exiting... in shutdown...
            }
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isPersistStep(Execution nextStepExecution) {
        //Here we check if we need to go to queue to persist the step context - we can do it with shortcut to InBuffer!!!!!!!!
        if (nextStepExecution.getSystemContext().isStepPersist()) {
            //clean the persist key
            nextStepExecution.getSystemContext().removeStepPersist();

            //set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();

            executionMessage.setStepPersist(true);
            executionMessage.setStepPersistId(nextStepExecution.getSystemContext().getStepPersistId());
            //clean the persist data
            nextStepExecution.getSystemContext().removeStepPersistID();

            //set the payload to the current step and not from the message that could be several micro step behind
            executionMessage.setPayload(converter.createPayload(nextStepExecution));

            ExecutionMessage inProgressMessage = createInProgressExecutionMessage(nextStepExecution);
            ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage, inProgressMessage}; //for the outBuffer

            ExecutionMessage inProgressMessageForInBuffer = (ExecutionMessage) inProgressMessage.clone();
            inProgressMessageForInBuffer.setPayload(null); //we do not need the payload for the inBuffer shortcut, we have execution there

            try {
                //The order is important!!!!!
                outBuffer.put(executionMessagesToSend);
                inBuffer.addExecutionMessage(inProgressMessageForInBuffer);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", e);
                return true; //exiting... in shutdown...
            }
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isSplitStep(Execution nextStepExecution){
        if(executionService.isSplitStep(nextStepExecution)){
            //set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();
            executionMessage.setPayload(null);

            ExecutionMessage pendingMessage = createPendingExecutionMessage(nextStepExecution);
            ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage, pendingMessage};//Messages that we will send to OutBuffer
            try {
                outBuffer.put(executionMessagesToSend);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", e);
                return true;
            }
            return true;
        }
        else {
            return false;
        }
    }

    private boolean shouldChangeWorkerGroup(Execution nextStepExecution) {
        //Here we check if we can continue to run in current thread - depends on the group
        if (nextStepExecution.getSystemContext().containsKey(TempConstants.SHOULD_CHECK_GROUP)) {
            //take care of worker group id
            String groupName = nextStepExecution.getGroupName();

            //clean key
            nextStepExecution.getSystemContext().remove(TempConstants.SHOULD_CHECK_GROUP);

            boolean canRunInThisWorker = groupName== null || //does not really matter on what worker to run
                                         workerConfigurationService.isMemberOf(groupName) || //this worker is member of the group
                                         isStickyToThisWorker(groupName); //next step should run in this worker because of "sticky worker" feature

            if(!canRunInThisWorker){
                //set current step to finished
                executionMessage.setStatus(ExecStatus.FINISHED);
                executionMessage.incMsgSeqId();
                executionMessage.setPayload(null);

                ExecutionMessage pendingMessage = createPendingExecutionMessage(nextStepExecution);
                ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage, pendingMessage};//Messages that we will send to OutBuffer
                try {
                    outBuffer.put(executionMessagesToSend);
                } catch (InterruptedException e) {
                    logger.warn("Thread was interrupted! Exiting the execution... ", e);
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    private boolean isStickyToThisWorker(String groupName){
        return (workerUUID != null && groupName.endsWith(workerUUID));
    }

    private boolean isOldThread() {

        boolean oldThread = !workerManager.isFromCurrentThreadPool(Thread.currentThread().getName());
        if(oldThread){ // interrupted old (recovery) thread
            if(logger.isDebugEnabled()) {
                logger.debug("This thread is from old thread pool...");
            }
            return true;
        }else {
            if(logger.isDebugEnabled()) {
                logger.debug("Execution was not interrupted and is in current thread pool! Continue... ");
            }
            return false;
        }
    }

    private boolean isExecutionCancelled(Execution execution) {

        if(isCancelledExecution(execution)) {
            if (logger.isDebugEnabled())  logger.debug("Execution is interrupted by Cancel");

            // NOTE: an execution can be cancelled directly from CancelExecutionService, if it's currently paused.
            // Thus, if you change the code here, please check CancelExecutionService as well.
            execution.getSystemContext().setFlowTerminationType(ExecutionStatus.CANCELED);
            execution.setPosition(null);

            //set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();
            executionMessage.setPayload(null);

            //Flow is finished - does not matter if successfully or not
            ExecutionMessage terminationMessage = createTerminatedExecutionMessage(execution);
            ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage, terminationMessage}; //Messages that we will send to OutBuffer

            try {
                outBuffer.put(executionMessagesToSend);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted While canceling! Exiting the execution... ", e);
            }
            return true;
        }

        return false;
    }

    private boolean isCancelledExecution(Execution execution) {

        if(execution==null) return false;

        boolean executionIsCancelled = workerConfigurationService.isExecutionCancelled(execution.getExecutionId()); // in this case - just check if need to cancel. It will set as cancelled later on QueueEventListener
        // Another scenario of getting canceled - it was cancelled from the SplitJoinService (the configuration can still be not updated). Defect #:22060
        if(ExecutionStatus.CANCELED.equals(execution.getSystemContext().getFlowTerminationType())) {
            executionIsCancelled = true;
        }

        return executionIsCancelled;
    }




    private boolean isRunningTooLong(Long startTime, Execution nextStepExecution) {
        Long currentTime = System.currentTimeMillis();

        //Return true if running more than 60 seconds.
        //We want to exit after 60 seconds from this thread in order to prevent starvation of other tasks.
        if ((currentTime - startTime) > 60 * 1000) {
            //set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();
            executionMessage.setPayload(null);

            ExecutionMessage inProgressMessage = createInProgressExecutionMessage(nextStepExecution);
            ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage, inProgressMessage}; //for the outBuffer

            ExecutionMessage inProgressMessageForInBuffer = (ExecutionMessage) inProgressMessage.clone();
            inProgressMessageForInBuffer.setPayload(null); //we do not need the payload for the inBuffer shortcut

            try {
                //The order is important!!!!!
                outBuffer.put(executionMessagesToSend);
                inBuffer.addExecutionMessage(inProgressMessageForInBuffer);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", e);
                return true; //exiting... in shutdown...
            }
            return true;

        } else {
            return false;
        }
    }

    // Creates termination execution message, base on current execution message
    private ExecutionMessage createTerminatedExecutionMessage(Execution nextStepExecution) {
        Payload payload = converter.createPayload(nextStepExecution); //we need the payload
        ExecutionMessage finalMessage = (ExecutionMessage) executionMessage.clone();
        finalMessage.setStatus(ExecStatus.TERMINATED); //in queue it is checked and finish flow is called
        finalMessage.incMsgSeqId();
        finalMessage.setPayload(payload);
        return finalMessage;
    }

    // Creates pending execution message for the next step, base on current execution message
    private ExecutionMessage createPendingExecutionMessage(Execution nextStepExecution) {
        //take care of worker group
        String groupName = nextStepExecution.getGroupName();
        if (groupName == null) {
            groupName = WorkerNode.DEFAULT_WORKER_GROUPS[0];
        }
        return new ExecutionMessage(ExecutionMessage.EMPTY_EXEC_STATE_ID,
                ExecutionMessage.EMPTY_WORKER,
                groupName,
                executionMessage.getMsgId(),
                ExecStatus.PENDING,
                converter.createPayload(nextStepExecution),
                0).setWorkerKey(executionMessage.getWorkerKey());
    }

    // Creates InProgress execution message for the next step, base on current execution message - used for short cut!
    private ExecutionMessage createInProgressExecutionMessage(Execution nextStepExecution) {
        //take care of worker group
        String groupName = nextStepExecution.getGroupName();
        if (groupName == null) {
            groupName = WorkerNode.DEFAULT_WORKER_GROUPS[0];
        }

        Long id = queueStateIdGeneratorService.generateStateId();
        // stay in the same worker in the next step
        return new ExecutionMessage(id,
                executionMessage.getWorkerId(),
                groupName,
                executionMessage.getMsgId(),
                ExecStatus.IN_PROGRESS,
                nextStepExecution,
                converter.createPayload(nextStepExecution),
                0).setWorkerKey(executionMessage.getWorkerKey());
    }


    private void executeSplitStep(Execution execution) throws InterruptedException {
        //If execution is paused or cancelled it will return false
        List<Execution> newExecutions = executionService.executeSplit(execution);

        //set current step to finished
        executionMessage.setStatus(ExecStatus.FINISHED);
        executionMessage.incMsgSeqId();
        executionMessage.setPayload(null);
        String splitId = getSplitId(newExecutions);
        SplitMessage splitMessage = new SplitMessage(splitId, execution, newExecutions);
        try {
            outBuffer.put(executionMessage, splitMessage);
        } catch (InterruptedException e) {
            logger.warn("Thread was interrupted! Exiting the execution... ", e);
        }
    }

    private boolean isExecutionTerminating(Execution nextStepExecution) {
        if(nextStepExecution.getPosition() == null) {
            //set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();
            executionMessage.setPayload(null);

            //Flow is finished - does not matter if successfully or not
            ExecutionMessage terminationMessage = createTerminatedExecutionMessage(nextStepExecution);
            ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage, terminationMessage}; //Messages that we will send to OutBuffer

            try {
                outBuffer.put(executionMessagesToSend);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", e);
                return true;
            }
            return true;
        }
        else {
            return false;
        }
    }

    private String getSplitId(List<Execution> newExecutions) {
        if (newExecutions != null && newExecutions.size() > 0) {
            return newExecutions.get(0).getSystemContext().getSplitId();
        }
        throw new RuntimeException("Split executions list is null or empty!!!");
    }
}
