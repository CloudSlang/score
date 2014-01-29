package com.hp.oo.engine.queue.services.recovery;

import com.hp.oo.engine.node.services.LoginListener;
import com.hp.oo.engine.node.services.WorkerNodeService;
import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.services.CounterNames;
import com.hp.oo.engine.queue.services.ExecutionQueueService;
import com.hp.oo.engine.versioning.services.VersionService;
import com.hp.oo.enginefacade.Worker;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Amit Levin
 * Date: 20/11/12
 */
final public class ExecutionRecoveryServiceImpl implements ExecutionRecoveryService, LoginListener {
	private final Logger logger = Logger.getLogger(getClass());

	static final int DEFAULT_POLL_SIZE = 1000;

    static final private long maxAllowedGap = Long.getLong("max.allowed.version.gap.msg.recovery",10); //This is the max allowed gap
    // of versions for msg acknowledge, please note that this param with the rate of the version job, determines the time gap for msg recovery!

	@Autowired
	private WorkerNodeService workerNodeService;

	@Autowired
	private ExecutionQueueService executionQueueService;

    @Autowired
    private VersionService versionService;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Override
	@Transactional(propagation = Propagation.NEVER)
	public void doRecovery() {
		if (logger.isDebugEnabled()) logger.debug("Begin recovery");
		recoverWorkers();

		recoverMessages();

		assignRecoveredMessages();
		if (logger.isDebugEnabled()) logger.debug("End recovery");
	}

	private void recoverWorkers(){
		if (logger.isDebugEnabled()) logger.debug("Workers recovery is being started");
		long time = System.currentTimeMillis();
		// Recovery all the non-responding workers.
		List<String> workerNames = workerNodeService.readNonRespondingWorkers();
		if (logger.isDebugEnabled()) logger.debug(workerNames.size() + " workers will be recovered");
		for(String workerName: workerNames){
			try {
				doWorkerRecovery(workerName);
			} catch (Exception ex){
				logger.error("Failed to recover worker [" + workerName + "]", ex);
			}
		}
		if (logger.isDebugEnabled()) logger.debug("Workers recovery is done in " + (System.currentTimeMillis()-time) + " ms");
	}

	private void recoverMessages(){
		if (logger.isDebugEnabled()) logger.debug("Messages recovery is being started");
		long time = System.currentTimeMillis();
		final AtomicBoolean shouldContinue = new AtomicBoolean(true);
		while (shouldContinue.get()) {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
					List<ExecutionMessage> messages = getMessagesWithoutAck(DEFAULT_POLL_SIZE);
					logMessageRecovery(messages);
					doMessageRecovery(messages);
					shouldContinue.set(messages != null && messages.size() == DEFAULT_POLL_SIZE);
				}
			});
		}
		if (logger.isDebugEnabled()) logger.debug("Messages recovery is done in " + (System.currentTimeMillis()-time) + " ms");
	}

	private void assignRecoveredMessages(){
		if (logger.isDebugEnabled()) logger.debug("Reassigning recovered messages is being started");
		long time = System.currentTimeMillis();
		final AtomicBoolean shouldContinue = new AtomicBoolean(true);
		while (shouldContinue.get()) {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
					List<ExecutionMessage> messages = executionQueueService.readMessagesByStatus(DEFAULT_POLL_SIZE, ExecStatus.RECOVERED);
					enqueueMessages(messages, ExecStatus.PENDING);
					shouldContinue.set(messages != null && messages.size() == DEFAULT_POLL_SIZE);
				}
			});
		}
		if (logger.isDebugEnabled()) logger.debug("Reassigning recovered messages is done in " + (System.currentTimeMillis()-time) + " ms");
	}

    private void logMessageRecovery(List<ExecutionMessage> messages) {
        if(!messages.isEmpty()){
            logger.warn("Will do MessageRecovery for " + messages.size() + " Messages. ");
            if(logger.isDebugEnabled()){
                for(ExecutionMessage msg:messages){
                    logger.debug("Will do MessageRecovery for messages with ExecStateId = " + msg.getExecStateId());
                }
            }
        }
    }

    @Override
	@Transactional(propagation = Propagation.NEVER)
	public void doWorkerRecovery(final String workerName) {
        logger.warn("Worker [" + workerName + "] is going to be recovered");
	    long time = System.currentTimeMillis();
		// change status to in_recovery
		workerNodeService.updateStatus(workerName, Worker.Status.IN_RECOVERY);

	    final AtomicBoolean shouldContinue = new AtomicBoolean(true);

		while (shouldContinue.get()) {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
					workerNodeService.lock(workerName);
					List<ExecutionMessage> messages = executionQueueService.poll(workerName, DEFAULT_POLL_SIZE,
							ExecStatus.ASSIGNED,
							ExecStatus.SENT,
							ExecStatus.IN_PROGRESS);
					doMessageRecovery(messages);

					if (messages == null || messages.size() < DEFAULT_POLL_SIZE){
						shouldContinue.set(false);
						workerNodeService.updateStatus(workerName, Worker.Status.RECOVERED);
					}
				}
			});
		}
	    if (logger.isDebugEnabled()) logger.debug("Worker [" + workerName + "] recovery id done in " + (System.currentTimeMillis()-time) + " ms");
	}

	@Override
	@Transactional
	public void preLogin(String uuid) {
		doWorkerRecovery(uuid);
	}

	@Override
	@Transactional
	public void postLogin(String uuid) {
		// Noting to-do
	}

	private void doMessageRecovery(List<ExecutionMessage> messages) {
		enqueueMessages(messages, ExecStatus.RECOVERED);
	}

	private void enqueueMessages(List<ExecutionMessage> messages, ExecStatus messageStatus) {
		for(ExecutionMessage msg:messages){
			msg.setStatus(messageStatus);
			msg.setWorkerId(ExecutionMessage.EMPTY_WORKER);
			msg.incMsgSeqId();
		}
		executionQueueService.enqueue(messages);
	}

	private List<ExecutionMessage> getMessagesWithoutAck(int maxSize) {
		if (logger.isDebugEnabled()) logger.debug("Getting messages without ack...");

        long systemVersion = versionService.getCurrentVersion(CounterNames.MSG_RECOVERY_VERSION.name());
        long minVersionAllowed = Math.max( systemVersion - maxAllowedGap , 0);
		List<ExecutionMessage> result = executionQueueService.pollMessagesWithoutAck(maxSize,minVersionAllowed);

		if (logger.isDebugEnabled()) logger.debug("Messages without ack found: " + result.size());
		return result;
	}
}
