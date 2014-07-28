package com.hp.oo.execution.services;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.oo.engine.node.services.WorkerNodeService;

/**
 * @author Dima Rassin
 * @author Avi Moradi
 * @since 06/11/2013
 * @version $Id$
 */
public class WorkerRecoveryManagerImpl implements WorkerRecoveryManager {

	protected static final Logger logger = Logger.getLogger(WorkerRecoveryManagerImpl.class);

	@Autowired
	private List<WorkerRecoveryListener> listeners;
	@Autowired
	protected WorkerNodeService workerNodeService;
	@Autowired
	private RetryTemplate retryTemplate;
	private Lock lock = new ReentrantLock();
	private boolean inRecovery;

	@Override
	public void doRecovery() {
		if(!lock.tryLock()) return;
		try {
			inRecovery = true;
			logger.warn("Worker recovery started");
			for(WorkerRecoveryListener listener : listeners) {
				try {
					listener.doRecovery();
				} catch(Exception ex) {
					logger.error("Failed on recovery", ex);
				}
			}
			if(logger.isDebugEnabled()) logger.debug("Listeners recovery is done");
			retryTemplate.retry(RetryTemplate.INFINITELY, 30 * 1000L, new RetryTemplate.RetryCallback() {

				@Override
				public void tryOnce() {
					if(logger.isDebugEnabled()) logger.debug("sending worker UP");
					workerNodeService.up(System.getProperty("worker.uuid"));
					if(logger.isDebugEnabled()) logger.debug("the worker is UP");
				}
			});
			inRecovery = false;
			logger.warn("Worker recovery is done");
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isInRecovery() {
		return inRecovery;
	}

}
