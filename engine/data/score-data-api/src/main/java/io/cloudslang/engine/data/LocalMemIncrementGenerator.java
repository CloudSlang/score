/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.data;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by IntelliJ IDEA.
 * User: froelica
 * Date: 4/25/13
 * Time: 11:03 AM
 */
public class LocalMemIncrementGenerator implements IdentifierGenerator, IdentityGenerator {

	private final Logger logger = Logger.getLogger(getClass());

    private long currentId;
    private Lock lock = new ReentrantLock();

    @Override
    public Long next() {
        return (Long) generate(null, null);
    }

    @Override
    public List<Long> bulk(int bulkSize) {
        if (logger.isDebugEnabled()) {
            logger.debug("Generating a bulk of: " + bulkSize + " DB ids");
        }
        lock.lock();
        try {
            List<Long> bulk = new ArrayList<>(bulkSize);
            for (int i = 0; i < bulkSize; i++) {
                bulk.add(next());
            }
            return bulk;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        if (logger.isDebugEnabled()) {
            logger.debug("Generating DB ids");
        }
        lock.lock();
        try {
            return ++currentId;
        } finally {
            lock.unlock();
        }
    }
}
