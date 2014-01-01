package com.hp.oo.execution.services;

import com.hp.oo.engine.node.services.WorkerNodeService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * User: wahnonm
 * Date: 12/08/13
 * Time: 10:46
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class WorkerRecoveryManagerImplTest {

    @Autowired
    private WorkerRecoveryManager workerRecoveryManager;

    @Autowired
    private WorkerNodeService workerNodeService;

    @Autowired
    private RetryTemplate retryTemplate;

    @Autowired
    private List<WorkerRecoveryListener> workerRecoveryListeners;

    @Configuration
    static class config {
        @Bean
        public WorkerRecoveryManager workerRecoveryManager(){
            return new WorkerRecoveryManagerImpl();
        }

        @Bean
        public WorkerRecoveryListener workerRecoveryListener(){
            return mock(WorkerRecoveryListener.class);
        }

        @Bean
        public WorkerRecoveryListener workerRecoveryListener2(){
            return mock(WorkerRecoveryListener.class);
        }

        @Bean
        public WorkerNodeService workerNodeService(){
            return mock(WorkerNodeService.class);
        }

        @Bean
        public RetryTemplate retryTemplate(){
            return mock(RetryTemplate.class);
        }
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDoRecovery() throws Exception {
        Assert.assertEquals(2,workerRecoveryListeners.size());

        workerRecoveryManager.doRecovery();

        verify(workerRecoveryListeners.get(0),times(1)).doRecovery();
        verify(workerRecoveryListeners.get(1),times(1)).doRecovery();
    }

    @Test
    public void testIsInRecovery() throws Exception {
        Assert.assertFalse(workerRecoveryManager.isInRecovery());
    }
}
