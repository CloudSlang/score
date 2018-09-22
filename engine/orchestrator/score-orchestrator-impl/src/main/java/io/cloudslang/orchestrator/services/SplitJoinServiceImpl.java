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

import ch.lambdaj.function.convert.Converter;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.orchestrator.entities.BranchContexts;
import io.cloudslang.orchestrator.entities.FinishedBranch;
import io.cloudslang.orchestrator.entities.SplitMessage;
import io.cloudslang.orchestrator.entities.SuspendedExecution;
import io.cloudslang.orchestrator.repositories.FinishedBranchJdbcRepository;
import io.cloudslang.orchestrator.repositories.FinishedBranchRepository;
import io.cloudslang.orchestrator.repositories.SuspendedExecutionsRepository;
import io.cloudslang.score.api.EndBranchDataContainer;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.facade.execution.ExecutionStatus;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.convert;
import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

public final class SplitJoinServiceImpl implements SplitJoinService {
    public static final int MEGA_BYTE_IN_BYTES = 1024 * 1024;
    private final Logger logger = Logger.getLogger(getClass());

    private final int BULK_SIZE = Integer.getInteger("splitjoin.job.bulk.size", 200);
    private final long FACTOR_MULTI_INSTANCE = Long.getLong("splitjoin.multiinstance.factor", 125L);

    @Autowired
    private SuspendedExecutionsRepository suspendedExecutionsRepository;

    @Autowired
    private FinishedBranchRepository finishedBranchRepository;

    @Autowired
    private QueueDispatcherService queueDispatcherService;

    @Autowired
    private FinishedBranchJdbcRepository finishedBranchJdbcRepository;

    @Autowired
    private ExecutionMessageConverter converter;

    /*
        converts an execution to a fresh execution message for triggering a new flow
     */
    private final Converter<Execution, ExecutionMessage> executionToStartExecutionMessage = new Converter<Execution, ExecutionMessage>() {
        @Override
        public ExecutionMessage convert(Execution execution) {
            return new ExecutionMessage(execution.getExecutionId().toString(),
                    converter.createPayload(execution));
        }
    };

    /*
        converts an execution to a finish branch entity
     */
    private final Converter<Execution, FinishedBranch> executionToFinishedBranch = new Converter<Execution, FinishedBranch>() {
        @Override
        public FinishedBranch convert(Execution execution) {
            boolean isBranchCancelled = ExecutionStatus.CANCELED.equals(execution.getSystemContext().getFlowTerminationType());
            return new FinishedBranch(execution.getExecutionId().toString(), execution.getSystemContext().getBranchId(), execution.getSystemContext().getSplitId(), execution.getSystemContext().getStepErrorKey(), new BranchContexts(isBranchCancelled, execution.getContexts(), execution.getSystemContext()));
        }
    };

    @Override
    @Transactional
    public void split(List<SplitMessage> splitMessages) {
        notNull(splitMessages, "split messages cannot be null");

        if (splitMessages.isEmpty())
            return;

        // these lists will be populated with values and inserted in bulk to the db
        List<ExecutionMessage> stepFinishMessages = new ArrayList<>();
        List<ExecutionMessage> branchTriggerMessages = new ArrayList<>();
        List<SuspendedExecution> suspendedParents = new ArrayList<>();

        for (SplitMessage splitMessage : splitMessages) {
            // 1. trigger all the child branches
            List<ExecutionMessage> childExecutionMessages = convert(splitMessage.getChildren(), executionToStartExecutionMessage);
            branchTriggerMessages.addAll(childExecutionMessages);

            // 2. suspend the parent
            suspendedParents.add(new SuspendedExecution(splitMessage.getParent().getExecutionId().toString(),
                    splitMessage.getSplitId(),
                    splitMessage.getChildren().size(),
                    splitMessage.getParent()));
        }

        List<ExecutionMessage> queueMessages = new ArrayList<>();
        queueMessages.addAll(branchTriggerMessages);
        queueMessages.addAll(stepFinishMessages);

        // write new branches and end of step messages to queue
        queueDispatcherService.dispatch(queueMessages);

        // save the suspended parent entities
        suspendedExecutionsRepository.save(suspendedParents);
    }

    @Override
    @Transactional
    public void endBranch(List<Execution> executions) {
        notNull(executions, "executions cannot be null");

        if (executions.isEmpty())
            return;

        for (Execution execution : executions) {
            if (logger.isDebugEnabled())
                logger.debug("finishing branch " + execution.getSystemContext().getBranchId() + " for execution " + execution.getExecutionId());
        }

        // get the split id's for a batch query
        List<String> splitIds = extract(executions, on(Execution.class).getSystemContext().getSplitId());

        // fetch all suspended executions
        List<SuspendedExecution> suspendedExecutions = suspendedExecutionsRepository.findBySplitIdIn(splitIds);
        Map<String, SuspendedExecution> suspendedMap = new HashMap<>();
        for (SuspendedExecution se : suspendedExecutions) {
            suspendedMap.put(se.getSplitId(), se);
        }

        // validate that the returned result from the query contains entities for each of the split id's we asked
        // each finished branch must have it's parent in the suspended table, it is an illegal state otherwise
        for (String splitId : splitIds) {
            if (!suspendedMap.containsKey(splitId)) {
                Long executionId = findExecutionId(executions, splitId);
                logger.error("Couldn't find suspended execution for split " + splitId + " execution id: " + executionId);
            }
        }

        // create a finished branch entity for each execution
        List<FinishedBranch> finishedBranches = convert(executions, executionToFinishedBranch);

        List<SuspendedExecution> suspendedExecutionsWithOneBranch = new ArrayList<>();

        // add each finished branch to it's parent
        for (FinishedBranch finishedBranch : finishedBranches) {
            SuspendedExecution suspendedExecution = suspendedMap.get(finishedBranch.getSplitId());
            if (suspendedExecution != null) {
                finishedBranch.connectToSuspendedExecution(suspendedExecution);

                //this is an optimization for subflow (also works for MI with one branch :) )
                if (suspendedExecution.getNumberOfBranches() == 1) {
                    suspendedExecutionsWithOneBranch.add(suspendedExecution);
                } else {
                    finishedBranchRepository.save(finishedBranch);
                }
            }
        }

        if (!suspendedExecutionsWithOneBranch.isEmpty()) {
            joinAndSendToQueue(suspendedExecutionsWithOneBranch);
        }
    }

    private Long findExecutionId(List<Execution> executions, String splitId) {
        for (Execution execution : executions) {
            if (execution.getSystemContext().getSplitId().equals(splitId)) {
                return execution.getExecutionId();
            }
        }
        return null;
    }

    @Override
    @Transactional
    public int joinFinishedSplits(int bulkSize) {

        // 1. Find all suspended executions that have all their branches ended
        int sum = 0;
        for (int i = 0; i < bulkSize; i++) {
            PageRequest pageRequest = new PageRequest(0, 1);
            List<SuspendedExecution> suspendedExecutions = suspendedExecutionsRepository.findFinishedSuspendedExecutions(pageRequest);
            sum += joinAndSendToQueue(suspendedExecutions);

        }
        return sum;

    }

    @Override
    @Transactional
    public void joinFinishedSplits() {
        try {
            joinFinishedSplits(BULK_SIZE);
        } catch (Exception ex) {
            logger.error("SplitJoinJob failed", ex);
        }
    }

    private int joinAndSendToQueue(List<SuspendedExecution> suspendedExecutions) {
        if (logger.isDebugEnabled()) {
            logger.debug("Joining finished branches, found " + suspendedExecutions.size() + " suspended executions with all branches finished");
        }

        // nothing to do here
        if (suspendedExecutions.isEmpty()) {
            return 0;
        }


        for (SuspendedExecution se : suspendedExecutions) {
            Execution exec = joinSplit(se);
            List<ExecutionMessage> message = new ArrayList<>(1);

            // 3. send the suspended execution back to the queue one by one
            message.add(executionToStartExecutionMessage.convert(exec));

            queueDispatcherService.dispatch(message);
        }

        // 4. delete the suspended executions from the suspended table
        suspendedExecutionsRepository.delete(suspendedExecutions);
        return suspendedExecutions.size();
    }

    private Execution joinSplit(SuspendedExecution suspendedExecution) {
        Execution exec = suspendedExecution.getExecutionObj();
        notNull(exec);
        int countOfFinishedBranchesForExecutionId = (int) finishedBranchRepository.getCountOfFinishedBranchesForExecutionId(suspendedExecution.getExecutionId());
        isTrue(suspendedExecution.getNumberOfBranches().equals(countOfFinishedBranchesForExecutionId),
                "Expected suspended execution "
                        + exec.getExecutionId() + " to have "
                        + suspendedExecution.getNumberOfBranches()
                        + "finished branches, but found " + countOfFinishedBranchesForExecutionId);

        final String suspendedExecutionId = suspendedExecution.getExecutionId();


        Collection<Long> sizeOfBlobs = finishedBranchJdbcRepository.getSizeOfBlob(newArrayList(suspendedExecutionId));
        long sizeOfBlob = getSizeOfFinishedBranches(sizeOfBlobs);

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long maxMemory = heapUsage.getMax() / MEGA_BYTE_IN_BYTES;
        long usedMemory = heapUsage.getUsed() / MEGA_BYTE_IN_BYTES;


        Serializable stepTypeValue = exec.getSystemContext().get("STEP_TYPE");
        boolean isMultiInstance = equalsIgnoreCase(((stepTypeValue != null) ? stepTypeValue.toString() : null),
                "MULTI_INSTANCE");

        // For multi instance check memory else proceed as usual
        if (!isMultiInstance || (((FACTOR_MULTI_INSTANCE * sizeOfBlob) / MEGA_BYTE_IN_BYTES) < (maxMemory - usedMemory))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Joining execution " + exec.getExecutionId());
            }
            List<FinishedBranch> finishedBranches = suspendedExecution.getFinishedBranches();
            boolean wasExecutionCancelled = false;
            ArrayList<EndBranchDataContainer> finishedContexts = new ArrayList<>(finishedBranches.size());
            for (FinishedBranch fb : finishedBranches) {
                finishedContexts.add(new EndBranchDataContainer(fb.getBranchContexts().getContexts(), fb.getBranchContexts().getSystemContext(), fb.getBranchException()));
                if (fb.getBranchContexts().isBranchCancelled()) {
                    wasExecutionCancelled = true;
                }
            }

            // 2. insert all of the branches into the parent execution
            exec.getSystemContext().setFinishedChildBranchesData(finishedContexts);
            //mark cancelled on parent
            if (wasExecutionCancelled) {
                exec.getSystemContext().setFlowTerminationType(ExecutionStatus.CANCELED);
            }
        } else {
            exec.getSystemContext().setFlowTerminationType(ExecutionStatus.CANCELED);
        }
        return exec;
    }

    private long getSizeOfFinishedBranches(Collection<Long> sizeOfBlobs) {
        long sizeOfBlob = 0L;
        for (Long value : sizeOfBlobs) {
            sizeOfBlob += value;
        }
        return sizeOfBlob;
    }
}
