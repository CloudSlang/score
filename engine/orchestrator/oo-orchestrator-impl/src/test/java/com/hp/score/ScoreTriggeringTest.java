package com.hp.score;

import com.hp.oo.broker.entities.RunningExecutionPlan;
import com.hp.oo.broker.services.RunningExecutionPlanService;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.services.QueueDispatcherService;
import com.hp.oo.internal.sdk.execution.ExecutionPlan;
import com.hp.score.engine.data.IdentityGenerator;
import com.hp.score.services.ExecutionStateService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: wahnonm
 * Date: 30/01/14
 * Time: 14:23
 */
public class ScoreTriggeringTest {

    @InjectMocks
    private ScoreTriggering scoreTrigger = new ScoreTriggeringImpl();

    @Mock
    private RunningExecutionPlanService runningExecutionPlanService;

    @Mock
    private IdentityGenerator idGenerator;

    @Mock
    private QueueDispatcherService queueDispatcher;

    @Mock
    private ExecutionMessageConverter executionMessageConverter;

    @Mock
    private ExecutionStateService executionStateService;

    @Before
    public void resetMocks() {
        MockitoAnnotations.initMocks(this);
        RunningExecutionPlan runningExecutionPlan = mock(RunningExecutionPlan.class);
        when(runningExecutionPlan.getId()).thenReturn(2L);
        when(runningExecutionPlanService.createRunningExecutionPlan((any(RunningExecutionPlan.class)))).thenReturn(runningExecutionPlan);
    }

    @Test
    public void testTrigger2() throws Exception {
        ExecutionPlan ep = new ExecutionPlan();
        ep.setBeginStep(1L);
        scoreTrigger.trigger(ep, new HashMap<String, Serializable>(), new HashMap<String, Serializable>(), 1L);

        verify(queueDispatcher, times(1)).dispatch(anyListOf(ExecutionMessage.class));
    }

    @Test
    public void testSaveOfRunningEP() throws Exception {
        ExecutionPlan ep = new ExecutionPlan();
        ep.setBeginStep(1L);
        scoreTrigger.trigger(ep, new HashMap<String, Serializable>(), new HashMap<String, Serializable>(), 1L);

        verify(runningExecutionPlanService, times(1)).getOrCreateRunningExecutionPlan(any(ExecutionPlan.class));
    }

    @Test
    public void testSaveOfRunningEPComplex() throws Exception {
        ExecutionPlan ep = new ExecutionPlan();
        Map<String, ExecutionPlan> dep = new HashMap<>();
        dep.put("subflowEP", new ExecutionPlan());
        ep.setDependencies(dep);
        ep.setBeginStep(1L);
        scoreTrigger.trigger(ep, new HashMap<String, Serializable>(), new HashMap<String, Serializable>(), 1L);

        verify(runningExecutionPlanService, times(2)).getOrCreateRunningExecutionPlan(any(ExecutionPlan.class));
    }

}
