package com.hp.score;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * User: wahnonm
 * Date: 22/01/14
 * Time: 17:01
 */
public class ScoreTest {

    @InjectMocks
    private Score score = new ScoreImpl();

    @Mock
    private ScoreTriggering scoreTriggering;

    @Before
    public void resetMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testTrigger() throws Exception {
        ExecutionPlan ep = new ExecutionPlan();
        ep.setBeginStep(1L);
        TriggeringProperties triggeringProperties = TriggeringProperties.create(ep);
        score.trigger(triggeringProperties);

        verify(scoreTriggering, times(1)).trigger(any(TriggeringProperties.class));
    }

    @Test
    public void testTrigger2() throws Exception {
        ExecutionPlan ep = new ExecutionPlan();
        ep.setBeginStep(1L);
        TriggeringProperties triggeringProperties = TriggeringProperties.create(ep);
        score.trigger(triggeringProperties);

        verify(scoreTriggering, times(1)).trigger(any(TriggeringProperties.class));
    }

}
