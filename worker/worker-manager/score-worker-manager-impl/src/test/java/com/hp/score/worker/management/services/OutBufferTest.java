//package com.hp.oo.execution.services;
//
//import com.hp.oo.engine.queue.entities.ExecutionMessage;
//import com.hp.oo.engine.queue.entities.Payload;
//import com.hp.oo.engine.queue.services.QueueDispatcherService;
//import junit.framework.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.mockito.Mockito.*;
//
///**
// * User: wahnonm
// * Date: 14/08/13
// * Time: 18:34
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration
//public class OutBufferTest {
//
//    @InjectMocks
//    OutBuffer outBuffer = new OutBuffer();
//
//    @Mock
//    private QueueDispatcherService queueDispatcher;
//
//    @Mock
//    private RetryTemplate retryTemplate;
//
//    @Mock
//    private WorkerRecoveryManagerImpl recoveryManager;
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//        outBuffer.init();
//    }
//
//    @Configuration
//    static class EmptyConfig {}
//
//
//    @Test
//    public void testPutAckExecutionMessagesNoPayload() throws Exception {
//        List<ExecutionMessage> msgList = new ArrayList<>();
//        msgList.add(new ExecutionMessage());
//        outBuffer.putAckExecutionMessages(msgList);
//
//        verify(recoveryManager, never()).isInRecovery();
//    }
//
//    @Test
//    public void testPutAckExecutionMessagesWithPayload() throws Exception {
//        when(recoveryManager.isInRecovery()).thenReturn(true);
//        List<ExecutionMessage> msgList = new ArrayList<>();
//        msgList.add(new ExecutionMessage());
//        msgList.get(0).setPayload(new Payload());
//        outBuffer.putAckExecutionMessages(msgList);
//
//        verify(recoveryManager, times(1)).isInRecovery();
//    }
//
//    @Test
//    public void testGracefulShutdown() throws Exception {
//        outBuffer.gracefulShutdown();
//        verify(recoveryManager, never()).doRecovery();
//    }
//
//    @Test
//    public void testDrainBufferPeriodically() throws Exception {
//        outBuffer.drainBufferPeriodically();
//        verify(queueDispatcher, never()).dispatch(anyList()); //buffer is empty
//    }
//
//    @Test
//    public void testGetBufferSize() throws Exception {
//        Assert.assertEquals(0,outBuffer.getBufferSize());
//    }
//
//    @Test
//    public void testDoRecovery() throws Exception {
//        outBuffer.doRecovery();
//    }
//}
