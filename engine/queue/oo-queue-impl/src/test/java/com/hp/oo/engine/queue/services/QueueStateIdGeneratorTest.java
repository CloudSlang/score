package com.hp.oo.engine.queue.services;

import com.hp.score.engine.data.IdentityGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * User: wahnonm
 * Date: 07/08/13
 * Time: 16:21
 */
public class QueueStateIdGeneratorTest {

    @Mock
    private IdentityGenerator<Long> identityGenerator;

    @InjectMocks
    private QueueStateIdGeneratorService queueStateIdGeneratorService =
            new QueueStateIdGeneratorServiceImpl();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void verifyIdentityGeneratorNextWasCalled() {
        queueStateIdGeneratorService.generateStateId();
        verify(identityGenerator, times(1)).next();
    }

    @Test
    public void verifyIdentityGeneratorBulkWasNotCalled() {
        queueStateIdGeneratorService.generateStateId();
        verify(identityGenerator, times(0)).bulk(anyInt());
    }
}
