package com.hp.oo.engine.queue.services.cleaner;

import com.hp.oo.engine.queue.repositories.ExecutionQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 14/10/13
 */
@Service
final public class QueueCleanerServiceImpl  implements QueueCleanerService{

    final private int BULK_SIZE = 500;

    @Autowired
   	private ExecutionQueueRepository executionQueueRepository;

    @Override
    @Transactional
    public Set<Long> getFinishedExecStateIds() {
        return executionQueueRepository.getFinishedExecStateIds();
    }

    @Override
    @Transactional
    public void cleanFinishedSteps(Set<Long> ids) {
        executionQueueRepository.deleteFinishedSteps(ids);
    }

}
