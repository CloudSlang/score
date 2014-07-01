package com.hp.oo.orchestrator.services;

import com.hp.oo.broker.services.RunningExecutionPlanService;
import com.hp.oo.engine.execution.events.services.ExecutionEventService;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.services.QueueDispatcherService;
import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.score.engine.data.IdentityGenerator;
import com.hp.score.services.ExecutionStateService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;

/**
 * User: zruya
 * Date: 10/06/13
 * Time: 13:58
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class OrchestratorServiceTest {
    @InjectMocks
    private OrchestratorService orchestratorService = new OrchestratorServiceImpl();

    @Mock
    private RunningExecutionPlanService runningExecutionPlanService;

    @Mock
    private ExecutionEventService executionEventService;

    @Mock
    private ExecutionStateService executionStateService;

    @Mock
    private QueueDispatcherService queueDispatcher;

    @Mock
    private ExecutionMessageConverter executionMessageConverter;

    @Mock
    private IdentityGenerator idGenerator;

    @Configuration
    static class EmptyConfig {}

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(idGenerator.next()).then(new Answer<Long>() {
            private Long counter = 0L;
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                return counter++;
            }
        });
    }

    @Test
    public void defaultLogLevelTest() {
        Execution execution = orchestratorService.createExecution(1234L, 1L, new ArrayList<String>(), null);
        String resultLogLevel = (String)execution.getSystemContext().get(ExecutionConstants.EXECUTION_EVENTS_LOG_LEVEL);
        Assert.assertEquals("When not specifying a log level, the default should be INFO", ExecutionEnums.LogLevel.INFO.name(),  resultLogLevel);

        execution = orchestratorService.createExecution(1234L, 1L, new ArrayList<String>(), ExecutionEnums.LogLevel.DEBUG);
        resultLogLevel = (String)execution.getSystemContext().get(ExecutionConstants.EXECUTION_EVENTS_LOG_LEVEL);
        Assert.assertEquals("When specifying a log level, it should be included in the execution", ExecutionEnums.LogLevel.DEBUG.name(),  resultLogLevel);

        execution = orchestratorService.createExecution(1234L, 1L, new ArrayList<String>(), ExecutionEnums.LogLevel.INFO);
        resultLogLevel = (String)execution.getSystemContext().get(ExecutionConstants.EXECUTION_EVENTS_LOG_LEVEL);
        Assert.assertEquals("When specifying a log level, it should be included in the execution", ExecutionEnums.LogLevel.INFO.name(),  resultLogLevel);

        execution = orchestratorService.createExecution(1234L, 1L, new ArrayList<String>(), ExecutionEnums.LogLevel.ERROR);
        resultLogLevel = (String)execution.getSystemContext().get(ExecutionConstants.EXECUTION_EVENTS_LOG_LEVEL);
        Assert.assertEquals("When specifying a log level, it should be included in the execution", ExecutionEnums.LogLevel.ERROR.name(),  resultLogLevel);
    }

}
