package com.hp.oo.execution.services;

import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.entities.Payload;
import com.hp.oo.engine.queue.services.QueueStateIdGeneratorService;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.oo.orchestrator.entities.SplitMessage;
import com.hp.oo.orchestrator.services.configuration.WorkerConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 19/12/12
 */
public class SimpleExecutionRunnable implements Runnable {

    private final Logger logger = Logger.getLogger(this.getClass());

    private static final int SUBFLOW_POSITION = -1;
    private static final int PARALLEL_POSITION = -2;

    private ExecutionService executionService;

    private OutboundBuffer outBuffer;

    private InBuffer inBuffer;

    private ExecutionMessageConverter converter;

    private EndExecutionCallback endExecutionCallback;

    private ExecutionMessage executionMessage;

    private AtomicBoolean recoveryFlag;

    private QueueStateIdGeneratorService queueStateIdGeneratorService;

    private String workerUUID;

    private WorkerConfigurationService workerConfigurationService;

    public SimpleExecutionRunnable(ExecutionService executionService,
                                   OutboundBuffer outBuffer,
                                   InBuffer inBuffer,
                                   ExecutionMessageConverter converter,
                                   EndExecutionCallback endExecutionCallback,
                                   QueueStateIdGeneratorService queueStateIdGeneratorService,
                                   String workerUUID,
                                   WorkerConfigurationService workerConfigurationService
    ) {
        this.executionService = executionService;
        this.outBuffer = outBuffer;
        this.inBuffer = inBuffer;
        this.converter = converter;
        this.endExecutionCallback = endExecutionCallback;
        this.queueStateIdGeneratorService = queueStateIdGeneratorService;
        this.workerUUID = workerUUID;
        this.workerConfigurationService = workerConfigurationService;
    }

    public SimpleExecutionRunnable setRecoveryFlag(AtomicBoolean recoveryFlag) {
        this.recoveryFlag = recoveryFlag;
        return this;
    }

    public ExecutionMessage getExecutionMessage() {
        return executionMessage;
    }

    public void setExecutionMessage(ExecutionMessage executionMessage) {
        this.executionMessage = executionMessage;
    }

    @Override
    public void run() {
        long t = System.currentTimeMillis();
        executionMessage = doRun();
        int counter = 1;
        while (executionMessage != null && (System.currentTimeMillis() - t) < 3000) {
            executionMessage = doRun();
            counter++;
        }

        if (executionMessage != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("go to InBuffer after " + counter + " steps");
            }
            inBuffer.addExecutionMessage(executionMessage);
        } else if (logger.isDebugEnabled()) {
            logger.debug("end of thread after " + counter + " steps");
        }
    }

    private ExecutionMessage doRun() {
        String executionId = executionMessage.getMsgId();

        String origThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(origThreadName + "_" + executionId);

        try {
            Execution execution = converter.extractExecution(executionMessage.getPayload());

            //Check which logic to trigger - regular execution or split
            if (executionService.isSplitStep(execution)) {
                executeSplitStep(execution);
            } else {
                return executeRegularStep(execution);
            }
        } catch (Exception ex) {
            logger.error("Error during execution!!!", ex);
            //set status FAILED
            executionMessage.setStatus(ExecStatus.FAILED);
            //send only one execution message back - the new one was not created because of error
            outBuffer.put(executionMessage);
        } finally {

            if (logger.isDebugEnabled()) {
                logger.debug("Worker has finished to work on execution: " + executionId);
            }
            Long executionIdL = null;
            if (!StringUtils.isEmpty(executionId)) {
                executionIdL = Long.valueOf(executionId);
            }
            endExecutionCallback.endExecution(executionIdL);
            Thread.currentThread().setName(origThreadName);
        }
        return null;
    }

    private ExecutionMessage executeRegularStep(Execution execution) throws IOException {
        ExecutionMessage returnValue = null;
        //Actually execute the step and get the execution object of the next step
        Execution nextStepExecution;
        // this do while loop was added to force the worker execute the next execution micro step in case we running under debugger mode
        // which force executionService.execute to exist so the transaction commit th events insertion into DB
        do {
            nextStepExecution = executionService.execute(execution);
        }
        while (nextStepExecution != null && !nextStepExecution.isMustGoToQueue() && !isExecutionTerminating(nextStepExecution) && !executionService.isSplitStep(nextStepExecution));

        if (logger.isDebugEnabled()) {
            logger.debug("Execution done");
            if (nextStepExecution != null) {
                logger.debug("Next position: " + nextStepExecution.getPosition());
            }
        }

        if (recoveryFlag.get()) {
            logger.warn("Worker is in recovery. Execution result will be dropped");
            returnValue = null;
        }

        //set current step to finished
        executionMessage.setStatus(ExecStatus.FINISHED);
        executionMessage.incMsgSeqId();
        executionMessage.setPayload(null);

        //execution was paused - send the FINISHED message - but don't send the PENDING for next step
        if (nextStepExecution == null) {
            outBuffer.put(executionMessage);
        } else {
            ExecutionMessage[] executionMessagesToSend = createMessagesToSend(executionMessage, nextStepExecution);

            // for finished status , we don't need the payload.
            // but for terminated we need the payload
            outBuffer.put(executionMessagesToSend);

            // check if a new step was created for stay in the worker
            if (executionMessagesToSend.length == 2 && executionMessagesToSend[1].getStatus() == ExecStatus.IN_PROGRESS) {
                returnValue = (ExecutionMessage) executionMessagesToSend[1].clone();
            }
        }
        return returnValue;
    }

    private void executeSplitStep(Execution execution) {
        //If execution is paused or cancelled it will return false
        List<Execution> newExecutions = executionService.executeSplit(execution);

        //set current step to finished
        executionMessage.setStatus(ExecStatus.FINISHED);
        executionMessage.incMsgSeqId();
        executionMessage.setPayload(null);
        String splitId = getSplitId(newExecutions);
        SplitMessage splitMessage = new SplitMessage(splitId, execution, newExecutions);
        outBuffer.put(executionMessage, splitMessage);
    }

    private boolean isExecutionTerminating(Execution execution) {
        return (execution.getPosition() == null || execution.getPosition() == -1L || execution.getPosition() == -2L);
    }

    // Prepares executionMessage from previous executionMessage and new Execution object
    private ExecutionMessage[] createMessagesToSend(ExecutionMessage executionMessage, Execution nextStepExecution) throws IOException {
        Long nextPosition = nextStepExecution.getPosition();

        //Flow is finished - does not matter if successfully or not
        if (nextPosition == null) {
            Payload payload = converter.createPayload(nextStepExecution);
            ExecutionMessage finalMessage = (ExecutionMessage) executionMessage.clone();
            finalMessage.setStatus(ExecStatus.TERMINATED);//in queue it is checked and finish flow is called
            finalMessage.incMsgSeqId();
            finalMessage.setPayload(payload);
            return new ExecutionMessage[]{executionMessage, finalMessage};
        }
        //Subflow was started or Parallel was started - this execution should be terminated
        else if (nextPosition == SUBFLOW_POSITION || nextPosition == PARALLEL_POSITION) {
            //we do not call here the finish flow - since it is not finished yet!!!
            return new ExecutionMessage[]{executionMessage};
        }

        ExecutionMessage nextExecutionMessage = prepareNextStepExecutionMessage(executionMessage, nextStepExecution);

        return new ExecutionMessage[]{executionMessage, nextExecutionMessage};
    }

    // Prepares executionMessage from previous executionMessage and new Execution object
    private ExecutionMessage prepareNextStepExecutionMessage(ExecutionMessage executionMessage, Execution nextStepExecution) throws IOException {

        //take care of worker group id
        String workerGroupId = nextStepExecution.getGroupName();
        if (workerGroupId == null) {
            workerGroupId = WorkerNode.DEFAULT_WORKER_GROUPS[0];
        }
        Object useStayInTheWorkerObj = nextStepExecution.getSystemContext().get(ExecutionConstants.USE_STAY_IN_THE_WORKER);
        nextStepExecution.getSystemContext().remove(ExecutionConstants.USE_STAY_IN_THE_WORKER);
        boolean useStayInTheWorker = (useStayInTheWorkerObj != null) && (useStayInTheWorkerObj.equals(Boolean.TRUE));

        boolean isSameWorker = workerConfigurationService.getWorkerGroups().contains(workerGroupId) ||
                (workerUUID != null && workerGroupId.endsWith(workerUUID));

        if (isSameWorker && useStayInTheWorker) {
            Long id = queueStateIdGeneratorService.generateStateId();
            // stay in the same worker in te next step
            return new ExecutionMessage(id,
                    executionMessage.getWorkerId(),
                    workerGroupId,
                    executionMessage.getMsgId(),
                    ExecStatus.IN_PROGRESS,
                    converter.createPayload(nextStepExecution),
                    0).setWorkerKey(executionMessage.getWorkerKey());
        } else {
            // need to move to anther worker
            return new ExecutionMessage(ExecutionMessage.EMPTY_EXEC_STATE_ID,
                    ExecutionMessage.EMPTY_WORKER,
                    workerGroupId,
                    executionMessage.getMsgId(),
                    ExecStatus.PENDING,
                    converter.createPayload(nextStepExecution),
                    0).setWorkerKey(executionMessage.getWorkerKey());
        }
    }

    private String getSplitId(List<Execution> newExecutions) {
        if (newExecutions != null && newExecutions.size() > 0) {
            return newExecutions.get(0).getSplitId();
        } else {
            throw new RuntimeException("Split executions list is null or empty!!!");
        }
    }

}
