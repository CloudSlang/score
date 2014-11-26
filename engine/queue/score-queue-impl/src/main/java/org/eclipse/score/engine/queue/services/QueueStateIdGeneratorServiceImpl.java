/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.engine.queue.services;

import org.eclipse.score.engine.data.IdentityGenerator;
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
