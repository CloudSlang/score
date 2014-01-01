package com.hp.oo.execution.gateways;

import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.execution.services.OutboundBuffer;
import com.hp.oo.internal.sdk.execution.Execution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * User: wahnonm
 * Date: 12/08/13
 * Time: 09:50
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ExecutionGatewayTest {


    @InjectMocks
    private ExecutionGateway executionGateway = new ExecutionGatewayImpl();

    @Mock
    private OutboundBuffer outBuffer;

    @Mock
    private ExecutionMessageConverter executionMessageConverter;

    @Configuration
    static class EmptyConfig {}

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddExecution() throws Exception {
        executionGateway.addExecution(new Execution());
        verify(outBuffer,times(1)).put(any(ExecutionMessage.class));
    }

}
