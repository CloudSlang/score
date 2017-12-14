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
import io.cloudslang.engine.queue.services.ExecutionQueueService;
import io.cloudslang.orchestrator.services.EngineVersionService;
import io.cloudslang.score.facade.entities.Execution;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 19/11/12
 */
final public class ExecutionAssignerServiceImpl implements ExecutionAssignerService {

	private Logger logger = Logger.getLogger(getClass());

	@Autowired
	private ExecutionQueueService executionQueueService;

	@Autowired
	private WorkerNodeService workerNodeService;

	@Autowired
	private ExecutionMessageConverter converter;

    @Autowired
    private EngineVersionService engineVersionService;


	private void addErrorMessage(ExecutionMessage message) {
        String group = message.getWorkerGroup();
        Execution execution = converter.extractExecution(message.getPayload());
        execution.getSystemContext().setNoWorkerInGroup(group);

        Payload payload = converter.createPayload(execution);
        message.setPayload(payload);
	}


	private void fillPayload(ExecutionMessage msg) {
		if (msg.getPayload() == null){
			Map<Long, Payload> payloadMap = executionQueueService.readPayloadByExecutionIds(msg.getExecStateId());
			Payload payload = payloadMap.get(msg.getExecStateId());
			msg.setPayload(payload);
		}
	}

    private String chooseWorker(String groupName, Multimap<String, String> groupWorkersMap,Random randIntGenerator) {
		Collection<String> workerNames = groupWorkersMap.get(groupName);

		if (workerNames == null || workerNames.size() == 0) {
			// this returns a worker UUID in case of the group defined on specific worker (private group)
			if (groupName.startsWith("Worker_")) {
				return groupName.substring("Worker_".length());
			} else {
				return null;
			}
		}

		Object[] workerArr = workerNames.toArray();

		// we assign the worker using random algorithm
		int groupIndex = randIntGenerator.nextInt(workerArr.length);
		groupIndex = groupIndex % workerArr.length;

		return (String) workerArr[groupIndex];
	}


    @Override
    @Transactional
    public List<ExecutionMessage> assignWorkers(List<ExecutionMessage> messages) {
        if (logger.isDebugEnabled()) logger.debug("Assigner iteration started");
        if (CollectionUtils.isEmpty(messages)) {
            if (logger.isDebugEnabled()) logger.debug("Assigner iteration finished");
            return messages;

        }
        List<ExecutionMessage> assignMessages = new ArrayList<>(messages.size());
        Multimap<String, String> groupWorkersMap  = null;
        Random randIntGenerator = new Random(System.currentTimeMillis());

        for (ExecutionMessage msg : messages) {

            if ( msg.getWorkerId().equals(ExecutionMessage.EMPTY_WORKER) && msg.getStatus() == ExecStatus.PENDING) {
                if (groupWorkersMap == null) {
                    String engineVersionId = engineVersionService.getEngineVersionId();
                    //We allow to assign to workers who's version is equal to the engine version
                    groupWorkersMap = workerNodeService.readGroupWorkersMapActiveAndRunningAndVersion(engineVersionId);
                }
                String workerId = chooseWorker(msg.getWorkerGroup(), groupWorkersMap,randIntGenerator);
                if (workerId == null) {
                    // error on assigning worker, no available worker
                    logger.warn("Can't assign worker for group name: " + msg.getWorkerGroup() + " , because there are no available workers for that group.");

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
            }
            else {
                // msg that was already assigned or non pending status
                assignMessages.add(msg);
            }
        } // end for

        if (logger.isDebugEnabled()) logger.debug("Assigner iteration finished");
        return assignMessages;
    }

}
