package com.hp.oo.engine.versioning.services;

import com.hp.oo.engine.versioning.entities.VersionCounter;
import com.hp.oo.engine.versioning.repositories.VersionRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * User: wahnonm
 * Date: 28/10/13
 * Time: 19:13
 */
@Service
public final class VersionServiceImpl implements VersionService {

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
