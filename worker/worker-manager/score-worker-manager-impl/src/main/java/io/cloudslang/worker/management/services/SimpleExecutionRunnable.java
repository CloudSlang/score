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
import org.apache.log4j.Logger;

import java.util.List;

import static java.lang.Boolean.getBoolean;
import static java.lang.Long.parseLong;
import static java.lang.Thread.currentThread;


public class SimpleExecutionRunnable implements Runnable {

    private static final Logger logger = Logger.getLogger(SimpleExecutionRunnable.class);

    private final ExecutionService executionService;

    private final OutboundBuffer outBuffer;

    private final InBuffer inBuffer;

    private final ExecutionMessageConverter converter;

    private final EndExecutionCallback endExecutionCallback;

    private ExecutionMessage executionMessage;

    private final QueueStateIdGeneratorService queueStateIdGeneratorService;

    private final String workerUUID;

    private final WorkerConfigurationService workerConfigurationService;

    private final boolean isRecoveryDisabled;

    private final WorkerManager workerManager;

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

        // System property - whether the executions are recoverable in case of restart/failure.
        this.isRecoveryDisabled = getBoolean("is.recovery.disabled");
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

        // We are renaming the thread for logging/monitoring purposes
        String origThreadName = currentThread().getName();
        currentThread().setName(origThreadName + "_" + executionId);
        Execution execution = null;
        try {
            // If we got here because of te shortcut we have the object
            // If we got here form DB - we need to extract the object from bytes
            execution = (executionMessage.getExecutionObject() != null) ? executionMessage.getExecutionObject()
                    : converter.extractExecution(executionMessage.getPayload());

            // Check which logic to trigger - regular execution or split
            if (executionService.isSplitStep(execution)) {
                executeSplitStep(execution);
            } else {
                executeRegularStep(execution);
            }
        } catch (InterruptedException interruptedException) {

            // Not old thread and interrupted by cancel
            if (workerManager.isFromCurrentThreadPool(currentThread().getName()) && isExecutionCancelled(execution)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Execution is interrupted...");
                }
            } else {
                logger.error("Execution thread is interrupted!!! Exiting...", interruptedException);
            }
        } catch (Exception ex) {
            logger.error("Error during execution!!!", ex);
            // Set status FAILED
            executionMessage.setStatus(ExecStatus.FAILED);
            executionMessage
                    .incMsgSeqId();    // New status must be with incremented msg_seq_id - otherwise will be recovered and we will get duplications
            // Send only one execution message back - the new one was not created because of error
            try {
                if (executionMessage.getPayload() == null) {
                    // This is done since we could get here from InBuffer shortcut - so no payload... and for FAILED message we need to set the payload
                    executionMessage.setPayload(converter.createPayload(execution));
                }
                outBuffer.put(executionMessage);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", ex);
            }
        } finally {
            endExecutionCallback.endExecution(parseLong(executionId));
            // Rename the thread back
            currentThread().setName(origThreadName);
        }
    }

    private void executeRegularStep(Execution execution) throws InterruptedException {
        Execution nextStepExecution;
        long startTime = System.currentTimeMillis();

        do {
            // Actually execute the step and get the execution object of the next step
            nextStepExecution = executionService.execute(execution);
        }
        while (!shouldStop(nextStepExecution, startTime));
    }

    private boolean shouldStop(Execution nextStepExecution, long startTime) {
        // We should stop if
        // 1. Thread was interrupted
        // 2. execution was paused
        // 3. we should stop and go to queue
        // 4. The execution is terminating
        // 5. The nextStepExecution is a splitStep
        // 6. Running too long

        // The order is important
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

    // If execution was paused it sends the current step with status FINISHED and that is all...
    private boolean isExecutionPaused(Execution nextStepExecution) {
        // If execution was paused
        if (nextStepExecution == null) {
            // Set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();
            executionMessage.setPayload(null);
            // Execution was paused - send only the FINISHED message!
            try {
                outBuffer.put(executionMessage);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", e);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isRecoveryCheckpoint(Execution nextStepExecution) {
        // Here we check if we need to go to queue to persist - we can do it with shortcut to InBuffer
        if (!isRecoveryDisabled && nextStepExecution.getSystemContext()
                .containsKey(TempConstants.IS_RECOVERY_CHECKPOINT)) {
            // Clean key
            nextStepExecution.getSystemContext().remove(TempConstants.IS_RECOVERY_CHECKPOINT);

            // Set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();
            executionMessage.setPayload(null);

            ExecutionMessage inProgressMessage = createInProgressExecutionMessage(nextStepExecution);
            ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage,
                    inProgressMessage}; // For the outBuffer

            ExecutionMessage inProgressMessageForInBuffer = (ExecutionMessage) inProgressMessage.clone();
            inProgressMessageForInBuffer.setPayload(null); // We do not need the payload for the inBuffer shortcut

            try {
                // The order is important
                outBuffer.put(executionMessagesToSend);
                inBuffer.addExecutionMessage(inProgressMessageForInBuffer);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", e);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isPersistStep(Execution nextStepExecution) {
        //Here we check if we need to go to queue to persist the step context - we can do it with shortcut to InBuffer!!!!!!!!
        if (nextStepExecution.getSystemContext().isStepPersist()) {
            // Clean the persist key
            nextStepExecution.getSystemContext().removeStepPersist();

            // Set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();

            executionMessage.setStepPersist(true);
            executionMessage.setStepPersistId(nextStepExecution.getSystemContext().getStepPersistId());
            // Clean the persist data
            nextStepExecution.getSystemContext().removeStepPersistID();

            // Set the payload to the current step and not from the message that could be several micro step behind
            executionMessage.setPayload(converter.createPayload(nextStepExecution));

            ExecutionMessage inProgressMessage = createInProgressExecutionMessage(nextStepExecution);
            ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage,
                    inProgressMessage}; // For the outBuffer

            ExecutionMessage inProgressMessageForInBuffer = (ExecutionMessage) inProgressMessage.clone();
            inProgressMessageForInBuffer
                    .setPayload(null); // We do not need the payload for the inBuffer shortcut, we have execution there

            try {
                // The order is important
                outBuffer.put(executionMessagesToSend);
                inBuffer.addExecutionMessage(inProgressMessageForInBuffer);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", e);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isSplitStep(Execution nextStepExecution) {
        if (executionService.isSplitStep(nextStepExecution)) {
            // Set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();
            executionMessage.setPayload(null);

            ExecutionMessage pendingMessage = createPendingExecutionMessage(nextStepExecution);
            ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage,
                    pendingMessage}; // Messages that we will send to OutBuffer
            try {
                outBuffer.put(executionMessagesToSend);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", e);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean shouldChangeWorkerGroup(Execution nextStepExecution) {
        // Here we check if we can continue to run in current thread - depends on the group
        if (nextStepExecution.getSystemContext().containsKey(TempConstants.SHOULD_CHECK_GROUP)) {
            // Take care of worker group id
            String groupName = nextStepExecution.getGroupName();

            // Clean key
            nextStepExecution.getSystemContext().remove(TempConstants.SHOULD_CHECK_GROUP);

            // Does not really matter on what worker to run
            // This worker is member of the group
            // Next step should run in this worker because of "sticky worker" feature
            boolean canRunInThisWorker = (groupName == null)
                    || workerConfigurationService.isMemberOf(groupName) || isStickyToThisWorker(groupName);

            if (!canRunInThisWorker) {
                //set current step to finished
                executionMessage.setStatus(ExecStatus.FINISHED);
                executionMessage.incMsgSeqId();
                executionMessage.setPayload(null);

                ExecutionMessage pendingMessage = createPendingExecutionMessage(nextStepExecution);
                ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage,
                        pendingMessage};//Messages that we will send to OutBuffer
                try {
                    outBuffer.put(executionMessagesToSend);
                } catch (InterruptedException e) {
                    logger.warn("Thread was interrupted! Exiting the execution... ", e);
                }
                return true;
            }
        }
        return false;
    }

    private boolean isStickyToThisWorker(String groupName) {
        return (workerUUID != null && groupName.endsWith(workerUUID));
    }

    private boolean isOldThread() {
        return !workerManager.isFromCurrentThreadPool(currentThread().getName());
    }

    private boolean isExecutionCancelled(Execution execution) {
        if (isCancelledExecution(execution)) {
            // NOTE: an execution can be cancelled directly from CancelExecutionService, if it's currently paused.
            // Thus, if you change the code here, please check CancelExecutionService as well.
            execution.getSystemContext().setFlowTerminationType(ExecutionStatus.CANCELED);
            execution.setPosition(null);

            //set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();
            executionMessage.setPayload(null);

            // Flow is finished - does not matter if successfully or not
            ExecutionMessage terminationMessage = createTerminatedExecutionMessage(execution);
            ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage,
                    terminationMessage}; // Messages that we will send to OutBuffer

            try {
                outBuffer.put(executionMessagesToSend);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted While canceling! Exiting the execution... ", e);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isCancelledExecution(Execution execution) {
        // In this case - just check if need to cancel. It will set as cancelled later on QueueEventListener
        // Another scenario of getting canceled - it was cancelled from the SplitJoinService (the configuration can still be not updated). Defect #:22060
        return (execution != null) && (workerConfigurationService.isExecutionCancelled(execution.getExecutionId())
                || (execution.getSystemContext().getFlowTerminationType() == ExecutionStatus.CANCELED));
    }


    private boolean isRunningTooLong(long startTime, Execution nextStepExecution) {

        // Return true if running more than 60 seconds. (this is not enforced, just a weak check)
        // to prevent starvation of other executions

        if ((System.currentTimeMillis() - startTime) > 60_000) {
            // Set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();
            executionMessage.setPayload(null);

            ExecutionMessage inProgressMessage = createInProgressExecutionMessage(nextStepExecution);
            ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage,
                    inProgressMessage}; // For the outBuffer

            ExecutionMessage inProgressMessageForInBuffer = (ExecutionMessage) inProgressMessage.clone();
            inProgressMessageForInBuffer.setPayload(null); // We do not need the payload for the inBuffer shortcut

            try {
                // The order is important
                outBuffer.put(executionMessagesToSend);
                inBuffer.addExecutionMessage(inProgressMessageForInBuffer);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", e);
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
        // Take care of worker group
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
        // Take care of worker group
        String groupName = nextStepExecution.getGroupName();
        if (groupName == null) {
            groupName = WorkerNode.DEFAULT_WORKER_GROUPS[0];
        }

        Long id = queueStateIdGeneratorService.generateStateId();
        // Stay in the same worker in the next step
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
        // If execution is paused or cancelled it will return false
        List<Execution> newExecutions = executionService.executeSplit(execution);

        // Set current step to finished
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
        if (nextStepExecution.getPosition() == null) {
            // Set current step to finished
            executionMessage.setStatus(ExecStatus.FINISHED);
            executionMessage.incMsgSeqId();
            executionMessage.setPayload(null);

            //Flow is finished - does not matter if successfully or not
            ExecutionMessage terminationMessage = createTerminatedExecutionMessage(nextStepExecution);
            ExecutionMessage[] executionMessagesToSend = new ExecutionMessage[]{executionMessage,
                    terminationMessage}; // Messages that we will send to OutBuffer

            try {
                outBuffer.put(executionMessagesToSend);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted! Exiting the execution... ", e);
            }
            return true;
        } else {
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
