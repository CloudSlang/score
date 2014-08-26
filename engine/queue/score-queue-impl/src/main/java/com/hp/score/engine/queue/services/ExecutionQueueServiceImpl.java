package com.hp.score.engine.queue.services;

import com.hp.score.engine.queue.entities.ExecStatus;
import com.hp.score.engine.queue.entities.ExecutionMessage;
import com.hp.score.engine.queue.entities.Payload;
import com.hp.score.engine.queue.repositories.ExecutionQueueRepository;
import com.hp.score.engine.queue.services.assigner.ExecutionAssignerService;
import com.hp.score.engine.versioning.services.VersionService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: Amit Levin
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
		long timeTotal = System.currentTimeMillis();
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
		long t = System.currentTimeMillis();
		if (stateMessages.size() > 0)
			executionQueueRepository.insertExecutionStates(stateMessages);

		long msgVersion = versionService.getCurrentVersion(CounterNames.MSG_RECOVERY_VERSION.name());
		executionQueueRepository.insertExecutionQueue(messages, msgVersion);
		if (logger.isDebugEnabled()) logger.debug("Persistency done in " + (System.currentTimeMillis() - t) + " ms");

		if (!CollectionUtils.isEmpty(listeners)) {
			t = System.currentTimeMillis();
			List<ExecutionMessage> failedMessages = filter(messages, ExecStatus.FAILED);
			List<ExecutionMessage> terminatedMessages = filter(messages, ExecStatus.TERMINATED);
			for (QueueListener listener : listeners) {
				listener.onEnqueue(messages, messages.size());
				if (failedMessages.size() > 0)
					listener.onFailed(failedMessages);
				if (terminatedMessages.size() > 0)
					listener.onTerminated(terminatedMessages);
			}
			if (logger.isDebugEnabled()) logger.debug("Listeners done in " + (System.currentTimeMillis() - t) + " ms");
		}
		if (logger.isDebugEnabled()) logger.debug("Enqueue done in " + (System.currentTimeMillis() - timeTotal) + " ms");
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
	public List<ExecutionMessage> poll(Date createDate, String workerId, int maxSize, ExecStatus... statuses) {
		List<ExecutionMessage> result = executionQueueRepository.poll(createDate, workerId, maxSize, statuses);

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
