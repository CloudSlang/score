package com.hp.oo.execution.services;

import com.hp.oo.engine.node.services.WorkerNodeService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Date: 6/11/13
 *
 * @author Dima Rassin
 */
public class WorkerRecoveryManagerImpl implements WorkerRecoveryManager {

	protected static final Logger logger = Logger.getLogger(WorkerRecoveryManagerImpl.class);

	@Autowired
	private List<WorkerRecoveryListener> listeners;
	@Autowired
	private WorkerNodeService workerNodeService;

	@Autowired
	private RetryTemplate retryTemplate;
	private Lock lock = new ReentrantLock();

	private volatile boolean inRecovery; //must be volatile since it is read/written in several threads

    private volatile String wrv; //must be volatile since it is read/written in several threads

	public void doRecovery(){
		if (!lock.tryLock()) return;
		try{
			inRecovery = true;
            logger.warn("Worker recovery started");
            for (WorkerRecoveryListener listener : listeners){
                try {
                    listener.doRecovery();
                } catch (Exception ex) {
                    logger.error("Failed on recovery", ex);
                }
            }
            if (logger.isDebugEnabled()) logger.debug("Listeners recovery is done");

            retryTemplate.retry(RetryTemplate.INFINITELY, 30*1000L, new RetryTemplate.RetryCallback() {
                @Override
                public void tryOnce() {
					if(logger.isDebugEnabled()) logger.debug("sending worker UP");
                    String newWrv = workerNodeService.up(System.getProperty("worker.uuid"));
                    setWRV(newWrv);
                    if(logger.isDebugEnabled()) logger.debug("the worker is UP");
                }
            });
            inRecovery = false;
            logger.warn("Worker recovery is done");
        } finally {
            lock.unlock();
		}
	}

	public boolean isInRecovery() {
		return inRecovery;
	}

    public String getWRV() {
        return wrv;
    }

    public void setWRV(String newWrv) {
        wrv = newWrv;
    }
}
