package com.hp.oo.orchestrator.services;

import com.hp.oo.broker.entities.RunningExecutionPlan;
import com.hp.oo.broker.services.RunningExecutionPlanService;
import com.hp.oo.engine.execution.events.services.ExecutionEventService;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.services.QueueDispatcherService;
import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.oo.internal.sdk.execution.ExecutionPlan;
import com.hp.oo.internal.sdk.execution.events.ExecutionEvent;
import com.hp.score.engine.data.IdentityGenerator;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    private ExecutionSummaryService executionSummaryService;

    @Mock
    private QueueDispatcherService queueDispatcher;

    @Mock
    private ExecutionMessageConverter executionMessageConverter;

    @Mock
    private RunningExecutionConfigurationService executionConfigurationService;

    @Mock
    private IdentityGenerator<Long> idGenerator;

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

    @Test
    public void testStartEventSentAtTrigger(){
        List<String> ctxNames = new ArrayList<>();
        ctxNames.add("flowCtx");
        orchestratorService.triggerFlow("", "", "", "", "flowCtx", "", "", new Execution(1L, 0L, ctxNames), new HashMap<String, String>());

        Mockito.verify(executionEventService,Mockito.times(1)).createEvents(Mockito.argThat(new ArgumentMatcher<List<ExecutionEvent>>() {
            @Override
            public boolean matches(Object o) {
                return ((List<ExecutionEvent>)o).get(0).getType().equals(ExecutionEnums.Event.START);   //make sure that the event is of type start event
            }
        }));
    }

    @Test
    public void testCreateRunningExecutionPlan(){
        ExecutionPlan executionPlan = new ExecutionPlan();
        RunningExecutionPlan runningExecutionPlan = new RunningExecutionPlan();
        runningExecutionPlan.setId(1L);

        when(runningExecutionPlanService.createRunningExecutionPlan(any(RunningExecutionPlan.class))).
                thenReturn(runningExecutionPlan);
        Long id = orchestratorService.getOrCreateRunningExecutionPlan(executionPlan);
        Assert.assertEquals((Long)1L,id);
    }

    @Test
    public void testGetRunningExecutionPlan(){
        ExecutionPlan executionPlan = new ExecutionPlan();
        RunningExecutionPlan runningExecutionPlan = new RunningExecutionPlan();
        runningExecutionPlan.setId(2L);
        runningExecutionPlan.setExecutionPlan(executionPlan);

        when(runningExecutionPlanService.readByFlowId(anyString())).thenReturn(Arrays.asList(runningExecutionPlan));
        Long id = orchestratorService.getOrCreateRunningExecutionPlan(executionPlan);
        Assert.assertEquals((Long)2L,id);
    }

    @Test
    public void testCreateRunningExecutionPlan2(){
        ExecutionPlan executionPlan = new ExecutionPlan();
        RunningExecutionPlan oldRunningExecutionPlan = new RunningExecutionPlan();
        oldRunningExecutionPlan.setId(3L);

        ExecutionPlan diffExecutionPlan = new ExecutionPlan();
        diffExecutionPlan.setFlowUuid("diff uuid");
        oldRunningExecutionPlan.setExecutionPlan(diffExecutionPlan);

        RunningExecutionPlan runningExecutionPlan = new RunningExecutionPlan();
        runningExecutionPlan.setId(5L);

        when(runningExecutionPlanService.readByFlowId(anyString())).thenReturn(Arrays.asList(oldRunningExecutionPlan));
        when(runningExecutionPlanService.createRunningExecutionPlan(any(RunningExecutionPlan.class))).
                thenReturn(runningExecutionPlan);

        Long id = orchestratorService.getOrCreateRunningExecutionPlan(executionPlan);
        Assert.assertEquals((Long) 5L, id);
    }
}
