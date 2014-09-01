package com.hp.score.worker.management.services;

import com.hp.score.engine.node.entities.WorkerNode;
import com.hp.score.worker.execution.services.ExecutionService;
import com.hp.score.engine.queue.entities.ExecStatus;
import com.hp.score.engine.queue.entities.ExecutionMessage;
import com.hp.score.engine.queue.entities.ExecutionMessageConverter;
import com.hp.score.engine.queue.entities.Payload;
import com.hp.score.engine.queue.services.QueueStateIdGeneratorService;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.score.orchestrator.entities.SplitMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.hp.score.worker.management.WorkerConfigurationService;

import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
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

        try {
            Execution execution = converter.extractExecution(executionMessage.getPayload());

            //Check which logic to trigger - regular execution or split
            if (executionService.isSplitStep(execution)) {
                executeSplitStep(execution);
            } else {
                executeRegularStep(execution);
            }
        }
        catch (Exception ex) {
            logger.error("Error during execution!!!", ex);
            //set status FAILED
            executionMessage.setStatus(ExecStatus.FAILED);
            //send only one execution message back - the new one was not created because of error
            outBuffer.put(executionMessage);
        }
        finally {
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

    private void executeRegularStep(Execution execution) throws IOException {
        Execution nextStepExecution;
        Long startTime = System.currentTimeMillis();

        try {
            do {
                //Actually execute the step and get the execution object of the next step
                nextStepExecution = executionService.execute(execution);
            }
            while (shouldContinue(nextStepExecution, startTime));
        }
        catch (InterruptedException ex){
            return; //Exit! The thread was interrupted by shutDown of the executor and was during Sleep or await() or any other method that supports InterruptedException
        }

        if(isInterrupted()){
            return; //Exit! The thread was interrupted by shutDown of the executor
        }

        //set current step to finished
        executionMessage.setStatus(ExecStatus.FINISHED);
        executionMessage.incMsgSeqId();
        executionMessage.setPayload(null);

        //execution was paused - send the FINISHED message - but don't send the PENDING for next step
        if (nextStepExecution == null) {
            outBuffer.put(executionMessage);
        }
        else {
            ExecutionMessage[] executionMessagesToSend = createMessagesToSend(executionMessage, nextStepExecution);

            // for finished status, we don't need the payload.
            // but for terminated we need the payload
            outBuffer.put(executionMessagesToSend);

            // check if a new step was created for stay in the worker
            if (executionMessagesToSend.length == 2 && executionMessagesToSend[1].getStatus() == ExecStatus.IN_PROGRESS) {
                ExecutionMessage inProgressMessage = (ExecutionMessage) executionMessagesToSend[1].clone();
                inBuffer.addExecutionMessage(inProgressMessage);
            }
        }
    }

    private boolean shouldContinue(Execution nextStepExecution, Long startTime){

        //We should continue to run the next step without exiting in following cases:
        //1. Thread was not interrupted
        //2. nextStepExecution is not null
        //3. nextStepExecution should not go to queue
        //4. The execution is not terminating
        //5. The nextStepExecution is not a splitStep
        //6. Not running too long

        return  !isInterrupted() &&
                nextStepExecution != null &&
                !nextStepExecution.isMustGoToQueue() &&
                !isExecutionTerminating(nextStepExecution) &&
                !executionService.isSplitStep(nextStepExecution) &&
                !isRunningTooLong(startTime);
    }

    private boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    private boolean isRunningTooLong(Long startTime){
        Long currentTime = System.currentTimeMillis();

        //Return true if running more than 3 seconds.
        //We want to exit after 3 seconds from this thread in order to prevent starvation of other tasks.
        return (currentTime - startTime) > 3000;
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
        return execution.getPosition() == null;
    }

    // Prepares executionMessage from previous executionMessage and new Execution object
    private ExecutionMessage[] createMessagesToSend(ExecutionMessage executionMessage, Execution nextStepExecution) throws IOException {
        //Flow is finished - does not matter if successfully or not
        if (isExecutionTerminating(nextStepExecution)) {
            Payload payload = converter.createPayload(nextStepExecution);
            ExecutionMessage finalMessage = (ExecutionMessage) executionMessage.clone();
            finalMessage.setStatus(ExecStatus.TERMINATED);//in queue it is checked and finish flow is called
            finalMessage.incMsgSeqId();
            finalMessage.setPayload(payload);
            return new ExecutionMessage[]{executionMessage, finalMessage};
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

        boolean isSameWorker = workerConfigurationService.isMemberOf(workerGroupId) ||
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
        }
		// need to move to anther worker
		return new ExecutionMessage(ExecutionMessage.EMPTY_EXEC_STATE_ID,
		        ExecutionMessage.EMPTY_WORKER,
		        workerGroupId,
		        executionMessage.getMsgId(),
		        ExecStatus.PENDING,
		        converter.createPayload(nextStepExecution),
		        0).setWorkerKey(executionMessage.getWorkerKey());
    }

    private String getSplitId(List<Execution> newExecutions) {
        if (newExecutions != null && newExecutions.size() > 0) {
            return newExecutions.get(0).getSplitId();
        }
		throw new RuntimeException("Split executions list is null or empty!!!");
    }
}
