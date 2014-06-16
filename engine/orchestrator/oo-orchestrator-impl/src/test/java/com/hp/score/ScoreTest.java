package com.hp.score;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.Score;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyMapOf;
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
        score.trigger(ep);

        verify(scoreTriggering, times(1)).trigger(any(ExecutionPlan.class), anyMapOf(String.class, Serializable.class), anyMapOf(String.class, Serializable.class), anyLong());
    }

    @Test
    public void testTrigger2() throws Exception {
        ExecutionPlan ep = new ExecutionPlan();
        ep.setBeginStep(1L);
        score.trigger(ep, new HashMap<String, Serializable>(), new HashMap<String, Serializable>(), 1L);

        verify(scoreTriggering, times(1)).trigger(any(ExecutionPlan.class), anyMapOf(String.class, Serializable.class), anyMapOf(String.class, Serializable.class), anyLong());
    }

}
