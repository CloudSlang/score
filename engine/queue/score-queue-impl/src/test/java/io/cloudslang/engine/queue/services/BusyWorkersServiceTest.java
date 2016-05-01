package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class BusyWorkersServiceTest {



    @Autowired
    private BusyWorkersService busyWorkersService;

    @Autowired
    private ExecutionQueueRepository executionQueueRepository;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        reset(executionQueueRepository);
    }

    @Test
    public void testIdleWorker(){
        List<String> busyWorkers = new ArrayList<>();
        when(executionQueueRepository.getBusyWorkers(ExecStatus.ASSIGNED)).thenReturn(busyWorkers);
        Assert.assertFalse(busyWorkersService.isWorkerBusy("worker1"));
    }

    @Test
    public void testBusyWorker(){
        List<String> busyWorkers = new ArrayList<>();
        busyWorkers.add("worker1");
        when(executionQueueRepository.getBusyWorkers(ExecStatus.ASSIGNED)).thenReturn(busyWorkers);
        busyWorkersService.findBusyWorkers();
        Assert.assertTrue(busyWorkersService.isWorkerBusy("worker1"));
    }


    @Configuration
    static class EmptyConfig {
        @Bean
        public BusyWorkersService busyWorkersService(){
            return new BusyWorkersServiceImpl();
        }

        @Bean
        public ExecutionQueueRepository executionQueueRepository(){
            return mock(ExecutionQueueRepository.class);
        }
    }
}
