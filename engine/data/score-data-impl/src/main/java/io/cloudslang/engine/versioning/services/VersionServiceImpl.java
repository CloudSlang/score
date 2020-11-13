/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.cloudslang.engine.versioning.services;

import io.cloudslang.engine.versioning.entities.VersionCounter;
import io.cloudslang.engine.versioning.repositories.VersionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private final Logger logger = LogManager.getLogger(this.getClass());

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
