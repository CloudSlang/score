/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.versioning.services;

import io.cloudslang.engine.versioning.entities.VersionCounter;
import io.cloudslang.engine.versioning.repositories.VersionRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * User: wahnonm
 * Date: 28/10/13
 * Time: 19:13
 */
public final class  VersionServiceImpl implements VersionService {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private VersionRepository versionRepository;

    @Override
    @Transactional
    @Cacheable("recovery_version")
    public long getCurrentVersion(String counterName) {
        VersionCounter versionCounter = versionRepository.findByCounterName(counterName);

        if(versionCounter == null){
            throw new IllegalStateException("No VersionCounter for counterName:"+counterName);
        }

        if(logger.isDebugEnabled()) logger.debug("got version :"+versionCounter+", for version named:"+counterName+" , at : "+new Date());

        return versionCounter.getVersionCount();
    }



    @Override
    @Transactional
    public void incrementVersion(String counterName) {
        int result = versionRepository.incrementCounterByName(counterName);
        if(result != 1){
            throw new IllegalStateException("for counterName:"+counterName+", got row count of :"+result);
        }
    }
}
