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

package io.cloudslang.engine.queue.services.assigner;

import com.google.common.collect.Multimap;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.enums.AssignStrategy;
import io.cloudslang.engine.queue.services.ExecutionQueueService;
import io.cloudslang.engine.queue.services.assigner.strategies.RandomStrategy;
import io.cloudslang.engine.queue.services.assigner.strategies.RoundRobinStrategy;
import io.cloudslang.engine.queue.services.assigner.strategies.SecureRandomStrategy;
import io.cloudslang.orchestrator.services.EngineVersionService;
import io.cloudslang.score.facade.entities.Execution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.cloudslang.engine.queue.enums.AssignStrategy.RANDOM;
import static io.cloudslang.engine.queue.enums.AssignStrategy.getAssignedStrategy;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

public final class ExecutionAssignerServiceImpl implements ExecutionAssignerService {

    private static final Logger logger = LogManager.getLogger(ExecutionAssignerServiceImpl.class);
    public static final String WORKER_MESSAGE_ASSIGNMENT_POLICY_KEY = "worker.messageAssignmentPolicy";
    private static final String WORKER_PREFIX = "Worker_";
    private static final int WORKER_PREFIX_LENGTH = WORKER_PREFIX.length();

    @Autowired
    private ExecutionQueueService executionQueueService;

    @Autowired
    private WorkerNodeService workerNodeService;

    @Autowired
    private ExecutionMessageConverter converter;

    @Autowired
    private EngineVersionService engineVersionService;

    private final AssignStrategy workerAssignStrategy;

    public ExecutionAssignerServiceImpl() {
        this.workerAssignStrategy = getAssignedStrategy(System.getProperty(WORKER_MESSAGE_ASSIGNMENT_POLICY_KEY),
                RANDOM);
        logger.info("Worker message assignment policy: " + workerAssignStrategy.getStrategyName());
    }

    private void addErrorMessage(ExecutionMessage message) {
        String group = message.getWorkerGroup();
        Execution execution = converter.extractExecution(message.getPayload());
        execution.getSystemContext().setNoWorkerInGroup(group);

        Payload payload = converter.createPayload(execution);
        message.setPayload(payload);
    }


    private void fillPayload(ExecutionMessage msg) {
        if (msg.getPayload() == null) {
            Map<Long, Payload> payloadMap = executionQueueService.readPayloadByExecutionIds(msg.getExecStateId());
            Payload payload = payloadMap.get(msg.getExecStateId());
            msg.setPayload(payload);
        }
    }

    private String chooseWorker(String groupName, Multimap<String, String> groupWorkersMap,
            ChooseWorkerStrategy assignWorkerStrategy) {
        Collection<String> workerNames = groupWorkersMap.get(groupName);

        if (isNotEmpty(workerNames)) {
            ArrayList<String> workerNamesAsList = new ArrayList<>(workerNames);
            // Please do not remove this, as this sorting is required in order to get predictable results
            Collections.sort(workerNamesAsList);
            int nextWorkerIndex = assignWorkerStrategy.getNextWorkerFromGroup(groupName, workerNamesAsList.size());
            return workerNamesAsList.get(nextWorkerIndex);
        } else {
            // This returns a worker UUID in case of the group defined on specific worker (private group)
            return groupName.startsWith(WORKER_PREFIX) ? groupName.substring(WORKER_PREFIX_LENGTH) : null;
        }
    }

    @Override
    @Transactional
    public List<ExecutionMessage> assignWorkers(List<ExecutionMessage> messages) {
        if (logger.isDebugEnabled()) {
            logger.debug("Assigner iteration started");
        }
        if (isEmpty(messages)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Assigner iteration finished");
            }
            return messages;

        }
        List<ExecutionMessage> assignMessages = new ArrayList<>(messages.size());
        Multimap<String, String> groupWorkersMap = null;

        ChooseWorkerStrategy chooseWorkerStrategy = createChooseWorkerStrategy();
        for (ExecutionMessage msg : messages) {

            if (msg.getWorkerId().equals(ExecutionMessage.EMPTY_WORKER) && msg.getStatus() == ExecStatus.PENDING) {
                if (groupWorkersMap == null) {
                    String engineVersionId = engineVersionService.getEngineVersionId();
                    //We allow to assign to workers who's version is equal to the engine version
                    groupWorkersMap = workerNodeService.readGroupWorkersMapActiveAndRunningAndVersion(engineVersionId);
                }
                String workerId = chooseWorker(msg.getWorkerGroup(), groupWorkersMap, chooseWorkerStrategy);
                if (workerId == null) {
                    // error on assigning worker, no available worker
                    logger.warn("Can't assign worker for group name: " + msg.getWorkerGroup()
                            + " , because there are no available workers for that group.");

                    //We need to extract the payload in case of FAILED
                    fillPayload(msg);

                    // send step finish event
                    ExecutionMessage stepFinishMessage = (ExecutionMessage) msg.clone();
                    stepFinishMessage.setStatus(ExecStatus.FINISHED);
                    stepFinishMessage.incMsgSeqId();
                    assignMessages.add(stepFinishMessage);

                    // send step finish event
                    ExecutionMessage flowFailedMessage = (ExecutionMessage) stepFinishMessage.clone();
                    flowFailedMessage.setStatus(ExecStatus.FAILED);
                    addErrorMessage(flowFailedMessage);
                    flowFailedMessage.incMsgSeqId();
                    assignMessages.add(flowFailedMessage);
                } else {
                    // assign worker
                    assignMessages.add(msg);
                    msg.setStatus(ExecStatus.ASSIGNED);
                    msg.incMsgSeqId();
                    msg.setWorkerId(workerId);
                }
            } else {
                // msg that was already assigned or non pending status
                assignMessages.add(msg);
            }
        } // end for

        if (logger.isDebugEnabled()) {
            logger.debug("Assigner iteration finished");
        }
        return assignMessages;
    }

    private ChooseWorkerStrategy createChooseWorkerStrategy() {
        switch (workerAssignStrategy) {
            case RANDOM:
                return new RandomStrategy();

            case SECURE_RANDOM:
                return new SecureRandomStrategy();

            case ROUND_ROBIN:
                return new RoundRobinStrategy();

            default:
                return new RandomStrategy();
        }
    }

}
