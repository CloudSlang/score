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

package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import io.cloudslang.engine.queue.services.assigner.ExecutionAssignerService;
import io.cloudslang.engine.versioning.services.VersionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * User:
 * Date: 20/09/12
 * Time: 18:09
 */
final public class ExecutionQueueServiceImpl implements ExecutionQueueService {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Autowired
	private ExecutionQueueRepository executionQueueRepository;

	@Autowired
	private ExecutionAssignerService executionAssignerService;

	@Autowired
	private BusyWorkersService busyWorkersService;

	@Autowired(required = false)
	private List<QueueListener> listeners = Collections.emptyList();

	@Autowired
	private VersionService versionService;

	@Override
	@Transactional
	public void enqueue(List<ExecutionMessage> messages) {
		if (CollectionUtils.isEmpty(messages))
			return;

		if (logger.isDebugEnabled()) logger.debug("Enqueue " + messages.size() + " messages");
		StopWatch stopWatch = new StopWatch();
        stopWatch.start();
		// assign worker for messages with pending status
		messages = executionAssignerService.assignWorkers(messages);
		if (logger.isDebugEnabled()) logger.debug("Messages were assigned successfully");

		final List<ExecutionMessage> stateMessages = new ArrayList<>(messages.size());

		// first fill the execution state id for new insert
		for (ExecutionMessage msg : messages) {
			if (msg.getExecStateId() == ExecutionMessage.EMPTY_EXEC_STATE_ID) {
				long execStateId = executionQueueRepository.generateExecStateId();
				msg.setExecStateId(execStateId);
				stateMessages.add(msg);
			} else if (msg.getPayload() != null && msg.getStatus() == ExecStatus.IN_PROGRESS) {
				stateMessages.add(msg);
			}
		}

        if (CollectionUtils.isNotEmpty(listeners)) {
            stopWatch.split();
            for (QueueListener listener : listeners) {
                listener.prePersist(messages);
            }
            if (logger.isDebugEnabled()) logger.debug("Listeners done in " + (stopWatch.getSplitTime()) + " ms");
        }

		stopWatch.split();
		if (stateMessages.size() > 0) {
			executionQueueRepository.insertExecutionStates(stateMessages);
			executionQueueRepository.insertNotActiveExecutionsQueues(stateMessages.stream()
					.filter(executionMessage -> !executionMessage.isActive())
					.collect(toList()));
		}

		long msgVersion = versionService.getCurrentVersion(VersionService.MSG_RECOVERY_VERSION_COUNTER_NAME);
		executionQueueRepository.insertExecutionQueue(messages, msgVersion);
		if (logger.isDebugEnabled()) logger.debug("Persistency done in " + (stopWatch.getSplitTime()) + " ms");

		if (CollectionUtils.isNotEmpty(listeners)) {
			stopWatch.split();
			List<ExecutionMessage> failedMessages = filter(messages, ExecStatus.FAILED);
			List<ExecutionMessage> terminatedMessages = filter(messages, ExecStatus.TERMINATED);
			List<ExecutionMessage> toPersistMessages = filterToPersistMessages(messages);
			for (QueueListener listener : listeners) {
				listener.onEnqueue(messages, messages.size());
				if (!failedMessages.isEmpty()){
					listener.onFailed(failedMessages);
				}
				if (!terminatedMessages.isEmpty()){
					listener.onTerminated(terminatedMessages);
				}
				if (!toPersistMessages.isEmpty()){
					listener.onPersistMessage(toPersistMessages);
				}
			}
			if (logger.isDebugEnabled()) logger.debug("Listeners done in " + (stopWatch.getSplitTime()) + " ms");
		}
		if (logger.isDebugEnabled()) logger.debug("Enqueue done in " + (stopWatch.getTime()) + " ms");
	}

	private List<ExecutionMessage> filter(List<ExecutionMessage> messages, ExecStatus status) {
		List<ExecutionMessage> result = new ArrayList<>();
		for (ExecutionMessage msg : messages) {
			if (msg.getStatus() == status) {
				result.add(msg);
			}
		}
		return result;
	}

	private List<ExecutionMessage> filterToPersistMessages(List<ExecutionMessage> messages) {
		List<ExecutionMessage> result = new ArrayList<>();
		for (ExecutionMessage msg : messages) {
			if (msg.isStepPersist()) {
				result.add(msg);
			}
		}
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ExecutionMessage> poll(String workerId, int maxSize, long workerPollingMemory, ExecStatus... statuses) {
		List<ExecutionMessage> result = new ArrayList<>();
		//check if the worker has work before actually polling for work
		if(busyWorkersService.isWorkerBusy(workerId))
			result = executionQueueRepository.poll(workerId, maxSize, workerPollingMemory, statuses);

		for (QueueListener listener : listeners) {
			listener.onPoll(result, result.size());
		}

		return result;
	}


	@Override
	@Transactional(readOnly = true)
	public List<ExecutionMessage> pollRecovery(String workerId, int maxSize, ExecStatus... statuses) {
		return executionQueueRepository.pollRecovery(workerId, maxSize, statuses);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ExecutionMessage> pollMessagesWithoutAck(int maxSize, long minVersionAllowed) {
		List<ExecutionMessage> result = executionQueueRepository.pollMessagesWithoutAck(maxSize, minVersionAllowed);
		if (listeners != null && result != null) {
			for (QueueListener listener : listeners) {
				listener.onPoll(result, result.size());
			}
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<Long, Payload> readPayloadByExecutionIds(Long... ids) {
		if (ArrayUtils.isEmpty(ids)) throw new IllegalArgumentException("List of IDs is null or empty");
		return executionQueueRepository.findPayloadByExecutionIds(ids);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ExecutionMessage> readMessagesByStatus(int maxSize, ExecStatus... statuses) {
		return executionQueueRepository.findByStatuses(maxSize, statuses);
	}

    @Override
    @Transactional(readOnly = true)
    public int countMessagesWithoutAckForWorker(int maxSize, long minVersionAllowed, String workerUuid) {
        return executionQueueRepository.countMessagesWithoutAckForWorker(maxSize, minVersionAllowed, workerUuid);
    }
}
