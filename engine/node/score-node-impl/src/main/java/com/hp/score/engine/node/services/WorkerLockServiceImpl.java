package com.hp.score.engine.node.services;

import com.hp.score.engine.node.entities.WorkerLock;
import com.hp.score.engine.node.repositories.WorkerLockRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * User: varelasa
 * Date: 20/07/14
 * Time: 11:27
 */
//@Service
public final class WorkerLockServiceImpl implements  WorkerLockService{

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private WorkerLockRepository workerLockRepository;

    @Override
    @Transactional
    public void create(String uuid) {

        WorkerLock workerLock = new WorkerLock();
        workerLock.setUuid(uuid);
        workerLockRepository.save(workerLock);
    }

    @Override
    @Transactional
    public void delete(String uuid) {

        workerLockRepository.deleteByUuid(uuid);
    }

    @Override
    @Transactional
    public void lock(String uuid) {
        if (workerLockRepository.lock(uuid) == 1){
            if (logger.isDebugEnabled()) logger.debug("Worker [" + uuid + "] is locked");
        }
        else {
            throw new IllegalStateException("Unknown worker uuid [" + uuid + "]");
        }
    }
}
