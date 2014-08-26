package com.hp.score.engine.versioning.services;

import com.hp.score.engine.versioning.entities.VersionCounter;
import com.hp.score.engine.versioning.repositories.VersionRepository;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;

/**
 * User: wahnonm
 * Date: 03/11/13
 * Time: 17:12
 */
@SuppressWarnings({"SpringContextConfigurationInspection"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class VersionServiceTest {

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private VersionService versionService;

    @After
    public void reset(){
        Mockito.reset(versionRepository);
    }

    @Test
    public void testGetCurrentVersion() throws Exception {
        VersionCounter versionCounter = new VersionCounter("counter1");
        when(versionRepository.findByCounterName("counter1")).thenReturn(versionCounter);

        long result = versionService.getCurrentVersion("counter1");
        Assert.assertEquals(0,result);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetCurrentVersionForNonExistentCounter() throws Exception {
        when(versionRepository.findByCounterName("newCounter")).thenReturn(null);

        versionService.getCurrentVersion("counter1");

    }

    @Test
    public void testIncrementVersion() throws Exception {
        when(versionRepository.incrementCounterByName("Counter")).thenReturn(1);
        versionService.incrementVersion("Counter");
        verify(versionRepository,times(1)).incrementCounterByName("Counter");
    }



    @Test(expected = IllegalStateException.class)
    public void testIncrementVersionWithOldCounterNameWithError() throws Exception {
        when(versionRepository.incrementCounterByName("oldCounter")).thenReturn(5);
        versionService.incrementVersion("oldCounter");
    }


    @Configuration
    static class Configurator {

        @Bean
        public VersionService versionService() {
            return new VersionServiceImpl();
        }

        @Bean
        public VersionRepository versionRepo() {
            return mock(VersionRepository.class);
        }
    }
}
