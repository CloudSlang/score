/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.engine.versioning.services;

import org.eclipse.score.engine.versioning.entities.VersionCounter;
import org.eclipse.score.engine.versioning.repositories.VersionRepository;
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
