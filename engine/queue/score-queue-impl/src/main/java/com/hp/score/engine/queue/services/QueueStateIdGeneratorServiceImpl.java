package com.hp.score.engine.queue.services;

import com.hp.score.engine.data.IdentityGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * User: sadane
 * Date: 09/10/13
 * Time: 08:54
 */
public final class QueueStateIdGeneratorServiceImpl implements QueueStateIdGeneratorService {

    @Autowired
    private IdentityGenerator identityGenerator;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Long generateStateId(){
        return (Long)identityGenerator.next();
    }
}
