/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.orchestrator.services;

import ch.lambdaj.function.convert.Converter;
import io.cloudslang.engine.queue.entities.StartNewBranchPayload;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import io.cloudslang.orchestrator.enums.SuspendedExecutionReason;
import io.cloudslang.score.api.EndBranchDataContainer;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.facade.execution.ExecutionStatus;
import io.cloudslang.orchestrator.entities.BranchContexts;
import io.cloudslang.orchestrator.entities.FinishedBranch;
import io.cloudslang.orchestrator.entities.SplitMessage;
import io.cloudslang.orchestrator.entities.SuspendedExecution;
import io.cloudslang.orchestrator.repositories.FinishedBranchRepository;
import io.cloudslang.orchestrator.repositories.SuspendedExecutionsRepository;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import static io.cloudslang.score.facade.execution.ExecutionStatus.CANCELED;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.convert;
import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.MULTI_INSTANCE;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.NON_BLOCKING;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.PARALLEL;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.PARALLEL_LOOP;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.FINISHED_CHILD_BRANCHES_DATA;
import static io.cloudslang.score.facade.TempConstants.MI_REMAINING_BRANCHES_CONTEXT_KEY;

import static java.lang.Long.parseLong;
import static java.lang.String.valueOf;
import static java.util.EnumSet.of;
import static java.util.stream.Collectors.toList;

public final class SplitJoinServiceImpl implements SplitJoinService {
    private final Logger logger = Logger.getLogger(getClass());

    private final Integer BULK_SIZE = Integer.getInteger("splitjoin.job.bulk.size", 200);

    @Autowired
    private SuspendedExecutionsRepository suspendedExecutionsRepository;

    @Autowired
    private FinishedBranchRepository finishedBranchRepository;

    @Autowired
    private QueueDispatcherService queueDispatcherService;

    @Autowired
    private ExecutionMessageConverter converter;

    @Autowired
    private ExecutionQueueRepository executionQueueRepository;

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
    private final Converter<Execution, FinishedBranch> executionToFinishedBranch = execution -> {
        boolean isBranchCancelled = CANCELED.equals(execution.getSystemContext().getFlowTerminationType());
        return new FinishedBranch(execution.getExecutionId().toString(), execution.getSystemContext().getBranchId(), execution.getSystemContext().getSplitId(), execution.getSystemContext().getStepErrorKey(), new BranchContexts(isBranchCancelled, execution.getContexts(), execution.getSystemContext()));
    };


    @Override
    @Transactional
    public void split(List<SplitMessage> splitMessages) {
        Validate.notNull(splitMessages, "split messages cannot be null");

        if (splitMessages.isEmpty())
            return;

        // these lists will be populated with values and inserted in bulk to the db
        List<ExecutionMessage> stepFinishMessages = new ArrayList<>();
        List<ExecutionMessage> branchTriggerMessages = new ArrayList<>();
        List<SuspendedExecution> suspendedParents = new ArrayList<>();

        for (SplitMessage splitMessage : splitMessages) {
            if (splitMessage.isExecutable()) {
                branchTriggerMessages.addAll(prepareExecutionMessages(splitMessage.getChildren(), true));

                Serializable stepTypeSerializable = splitMessage.getParent()
                        .getSystemContext().get("STEP_TYPE");
                final SuspendedExecutionReason stepType = stepTypeSerializable != null ?
                        SuspendedExecutionReason.valueOf(stepTypeSerializable.toString()) :
                        PARALLEL_LOOP;

                suspendedParents.add(new SuspendedExecution(splitMessage.getParent().getExecutionId().toString(),
                        splitMessage.getSplitId(),
                        splitMessage.getTotalNumberOfBranches(),
                        splitMessage.getParent(),
                        stepType,
                        false));
            } else {
                branchTriggerMessages.addAll(prepareExecutionMessages(splitMessage.getChildren(), false));
            }
        }

        List<ExecutionMessage> queueMessages = new ArrayList<>();
        queueMessages.addAll(branchTriggerMessages);
        queueMessages.addAll(stepFinishMessages);

        // write new branches and end of step messages to queue
        queueDispatcherService.dispatch(queueMessages);

        // save the suspended parent entities
        suspendedExecutionsRepository.save(suspendedParents);
    }

    private List<ExecutionMessage> prepareExecutionMessages(List<Execution> executions, boolean active) {
        return executions.stream()
                .map(execution -> {
                    ExecutionMessage executionMessage = new ExecutionMessage(execution.getExecutionId().toString(),
                            converter.createPayload(execution));
                    executionMessage.setActive(active);
                    return executionMessage;
                })
                .collect(toList());
    }

    @Override
    @Transactional
    public void endBranch(List<Execution> executions) {
        Validate.notNull(executions, "executions cannot be null");

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
        List<SuspendedExecution> suspendedExecutionsForMiWithOneBranch = new ArrayList<>();

        // add each finished branch to it's parent
        for (FinishedBranch finishedBranch : finishedBranches) {
            SuspendedExecution suspendedExecution = suspendedMap.get(finishedBranch.getSplitId());
            if (suspendedExecution != null) {
                finishedBranch.connectToSuspendedExecution(suspendedExecution);
                if (suspendedExecution.getSuspensionReason() == MULTI_INSTANCE) {
                    // start a new branch
                //    startNewBranch(suspendedExecution);

                    if (!finishedBranch.getBranchContexts().isBranchCancelled()) {
                        startNewBranch(suspendedExecution);
                    }
                    processFinishedBranch(finishedBranch, suspendedExecution, suspendedExecutionsForMiWithOneBranch);
                } else {
                    processFinishedBranch(finishedBranch, suspendedExecution, suspendedExecutionsWithOneBranch);
                }
            }
        }

        if (!suspendedExecutionsWithOneBranch.isEmpty()) {
            joinAndSendToQueue(suspendedExecutionsWithOneBranch);
        }
        if (!suspendedExecutionsForMiWithOneBranch.isEmpty()) {
            joinMiBranchesAndSendToQueue(suspendedExecutionsForMiWithOneBranch);
        }
    }

    private void processFinishedBranch(FinishedBranch finishedBranch,
                                       SuspendedExecution suspendedExecution,
                                       List<SuspendedExecution> suspendedExecutionsWithOneBranch) {
        if (suspendedExecution.getNumberOfBranches() == 1) {
            suspendedExecutionsWithOneBranch.add(suspendedExecution);
        } else {
            finishedBranchRepository.save(finishedBranch);
        }
    }

    private void startNewBranch(final SuspendedExecution suspendedExecution) {
        StartNewBranchPayload startNewBranchPayload = executionQueueRepository.getFirstPendingBranch(parseLong(suspendedExecution.getExecutionId()));
        if (startNewBranchPayload != null) {
            executionQueueRepository.activatePendingExecutionStateForAnExecution(startNewBranchPayload.getPendingExecutionStateId());
            executionQueueRepository.deletePendingExecutionState(startNewBranchPayload.getPendingExecutionMapingId());
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
        PageRequest pageRequest = new PageRequest(0, bulkSize);
        List<SuspendedExecution> suspendedExecutions = suspendedExecutionsRepository.findFinishedSuspendedExecutions(of(PARALLEL, NON_BLOCKING, PARALLEL_LOOP), pageRequest);

        return joinAndSendToQueue(suspendedExecutions);
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

    @Override
    @Transactional
    public int joinFinishedMiBranches(int bulkSize) {
        // 1. Find all suspended executions that have all their branches ended
        PageRequest pageRequest = new PageRequest(0, bulkSize);
        List<SuspendedExecution> suspendedExecutions = suspendedExecutionsRepository.findUnmergedSuspendedExecutions(of(MULTI_INSTANCE), pageRequest);

        return joinMiBranchesAndSendToQueue(suspendedExecutions);
    }

    private int joinMiBranchesAndSendToQueue(List<SuspendedExecution> suspendedExecutions) {
        List<ExecutionMessage> messages = new ArrayList<>();
        List<SuspendedExecution> mergedSuspendedExecutions = new ArrayList<>();

        if (logger.isDebugEnabled()) {
            logger.debug("Joining finished branches, found " + suspendedExecutions.size() + " suspended executions with all branches finished");
        }

        // nothing to do here
        if (suspendedExecutions.isEmpty())
            return 0;

        for (SuspendedExecution se : suspendedExecutions) {
            Execution execution = se.getExecutionObj();
            execution.getSystemContext().remove(FINISHED_CHILD_BRANCHES_DATA);
            execution.getSystemContext().put("CURRENT_PROCESSED__SPLIT_ID", se.getSplitId());
            joinMiSplit(se, execution);
            List<FinishedBranch> finishedBranches = se.getFinishedBranches();
            long nrOfNewFinishedBranches = finishedBranches.stream()
                    .filter(finishedBranch -> !finishedBranch.getBranchContexts().isBranchCancelled())
                    .count();
            se.setMergedBranches(se.getMergedBranches() + nrOfNewFinishedBranches);
            execution.getSystemContext().put(MI_REMAINING_BRANCHES_CONTEXT_KEY, valueOf(se.getNumberOfBranches() - se.getMergedBranches()));
            if (se.getMergedBranches() == se.getNumberOfBranches()) {
                mergedSuspendedExecutions.add(se);
            } else {
                se.setLocked(true);
                finishedBranches.clear();
            }
            messages.add(executionToStartExecutionMessage.convert(execution));
        }

        queueDispatcherService.dispatch(messages);

        suspendedExecutionsRepository.delete(mergedSuspendedExecutions);

        return suspendedExecutions.size();
    }

    private int joinAndSendToQueue(List<SuspendedExecution> suspendedExecutions) {
        List<ExecutionMessage> messages = new ArrayList<>();

        if (logger.isDebugEnabled())
            logger.debug("Joining finished branches, found " + suspendedExecutions.size() + " suspended executions with all branches finished");

        // nothing to do here
        if (suspendedExecutions.isEmpty())
            return 0;

        for (SuspendedExecution se : suspendedExecutions) {
            Execution exec = joinSplit(se);
            messages.add(executionToStartExecutionMessage.convert(exec));
        }

        // 3. send the suspended execution back to the queue
        queueDispatcherService.dispatch(messages);

        // 4. delete the suspended execution from the suspended table
        suspendedExecutionsRepository.delete(suspendedExecutions);

        return suspendedExecutions.size();
    }

    private Execution joinSplit(SuspendedExecution suspendedExecution) {

        List<FinishedBranch> finishedBranches = suspendedExecution.getFinishedBranches();
        Execution exec = suspendedExecution.getExecutionObj();

        Validate.isTrue(suspendedExecution.getNumberOfBranches().equals(finishedBranches.size()),
                "Expected suspended execution " + exec.getExecutionId() + " to have " + suspendedExecution.getNumberOfBranches() + "finished branches, but found " + finishedBranches.size());

        if (logger.isDebugEnabled())
            logger.debug("Joining execution " + exec.getExecutionId());

        boolean wasExecutionCancelled = false;
        ArrayList<EndBranchDataContainer> finishedContexts = new ArrayList<>();
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
            exec.getSystemContext().setFlowTerminationType(CANCELED);
        }

        return exec;
    }

    private void joinMiSplit(SuspendedExecution suspendedExecution, Execution exec) {
        List<FinishedBranch> finishedBranches = suspendedExecution.getFinishedBranches();

        if (logger.isDebugEnabled()) {
            logger.debug("Joining execution " + exec.getExecutionId());
        }

        boolean wasExecutionCancelled = false;
        ArrayList<EndBranchDataContainer> finishedContexts = new ArrayList<>();
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
            exec.getSystemContext().setFlowTerminationType(CANCELED);
        }
    }
}
