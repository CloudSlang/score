/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

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
		if (stateMessages.size() > 0)
			executionQueueRepository.insertExecutionStates(stateMessages);

		long msgVersion = versionService.getCurrentVersion(VersionService.MSG_RECOVERY_VERSION_COUNTER_NAME);
		executionQueueRepository.insertExecutionQueue(messages, msgVersion);
		if (logger.isDebugEnabled()) logger.debug("Persistency done in " + (stopWatch.getSplitTime()) + " ms");

		if (CollectionUtils.isNotEmpty(listeners)) {
			stopWatch.split();
			List<ExecutionMessage> failedMessages = filter(messages, ExecStatus.FAILED);
			List<ExecutionMessage> terminatedMessages = filter(messages, ExecStatus.TERMINATED);
			for (QueueListener listener : listeners) {
				listener.onEnqueue(messages, messages.size());
				if (failedMessages.size() > 0)
					listener.onFailed(failedMessages);
				if (terminatedMessages.size() > 0)
					listener.onTerminated(terminatedMessages);
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

	@Override
	@Transactional(readOnly = true)
	public List<ExecutionMessage> poll_(String workerId, int maxSize, ExecStatus... statuses) {
		List<ExecutionMessage> result = executionQueueRepository.poll_(workerId, maxSize, statuses);

		for (QueueListener listener : listeners) {
			listener.onPoll(result, result.size());
		}

		return result;
	}


	@Override
	@Transactional(readOnly = true)
	public List<ExecutionMessage> poll(String workerId, int maxSize, ExecStatus... statuses) {
		return executionQueueRepository.poll(workerId, maxSize, statuses);
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
