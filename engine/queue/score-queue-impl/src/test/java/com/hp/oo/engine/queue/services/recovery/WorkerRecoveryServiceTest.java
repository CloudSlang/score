package com.hp.oo.engine.queue.services.recovery;

import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.oo.engine.node.services.WorkerLockService;
import com.hp.oo.engine.node.services.WorkerNodeService;
import com.hp.oo.engine.queue.services.CounterNames;
import com.hp.oo.engine.queue.services.ExecutionQueueService;
import com.hp.oo.engine.versioning.services.VersionService;
import com.hp.oo.enginefacade.Worker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 8/6/14
 * Time: 10:21 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class WorkerRecoveryServiceTest {

    @InjectMocks
    private WorkerRecoveryService workerRecoveryService = new WorkerRecoveryServiceImpl();

    @Mock
    private WorkerNodeService workerNodeService;

    @Mock
    private WorkerLockService workerLockService;

    @Mock
    private VersionService versionService;

    @Mock
    private ExecutionQueueService executionQueueService;

    @Mock
    private MessageRecoveryService messageRecoveryService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(versionService.getCurrentVersion(CounterNames.MSG_RECOVERY_VERSION.name())).thenReturn(0L);
    }

    @Test
    //Test the situation when the worker is responsive and has no not acked messages
    public void testDoWorkerAndMessageRecoveryResponsiveAndNoMessages() throws Exception {
        when(workerNodeService.readNonRespondingWorkers()).thenReturn(Collections.<String>emptyList());
        when(executionQueueService.countMessagesWithoutAckForWorker(anyInt(), anyLong(), anyString())).thenReturn(0);
        WorkerNode mockWorker = mock(WorkerNode.class);
        when(mockWorker.getStatus()).thenReturn(Worker.Status.RUNNING);
        when(workerNodeService.findByUuid("123")).thenReturn(mockWorker);
        workerRecoveryService.doWorkerAndMessageRecovery("123");

        //Make sure the methods did not run
        verify(workerNodeService, never()).updateStatusInSeparateTransaction("123", Worker.Status.IN_RECOVERY);
    }

    @Test
    //Test the situation when the worker is non responsive and has no not acked messages
    public void testDoWorkerAndMessageRecoveryNonResponsive() throws Exception {
        when(workerNodeService.readNonRespondingWorkers()).thenReturn(getNonResponsiveWorkers());
        when(executionQueueService.countMessagesWithoutAckForWorker(anyInt(), anyLong(), anyString())).thenReturn(0);
        WorkerNode mockWorker = mock(WorkerNode.class);
        when(mockWorker.getStatus()).thenReturn(Worker.Status.RUNNING);
        when(workerNodeService.findByUuid("123")).thenReturn(mockWorker);
        workerRecoveryService.doWorkerAndMessageRecovery("123");
        //Make sure the methods did run
        verify(workerNodeService, times(1)).updateStatusInSeparateTransaction("123", Worker.Status.IN_RECOVERY);
    }

    @Test
    //Test the situation when the worker is responsive and has not acked messages
    public void testDoWorkerAndMessageRecoveryResponsiveAndHasMessages() throws Exception {
        when(workerNodeService.readNonRespondingWorkers()).thenReturn(Collections.<String>emptyList());
        when(executionQueueService.countMessagesWithoutAckForWorker(anyInt(), anyLong(), anyString())).thenReturn(10);
        WorkerNode mockWorker = mock(WorkerNode.class);
        when(mockWorker.getStatus()).thenReturn(Worker.Status.RUNNING);
        when(workerNodeService.findByUuid("123")).thenReturn(mockWorker);
        workerRecoveryService.doWorkerAndMessageRecovery("123");

        //Make sure the methods did run
        verify(workerNodeService, times(1)).updateStatusInSeparateTransaction("123", Worker.Status.IN_RECOVERY);
    }

    @Test
    //Test the situation when the worker is responsive, in recovery and has no not acked messages
    public void testDoWorkerAndMessageRecoveryResponsiveInRecoveryAndHasMessages() throws Exception {
        when(workerNodeService.readNonRespondingWorkers()).thenReturn(Collections.<String>emptyList());
        when(executionQueueService.countMessagesWithoutAckForWorker(anyInt(), anyLong(), anyString())).thenReturn(0);
        WorkerNode mockWorker = mock(WorkerNode.class);
        when(mockWorker.getStatus()).thenReturn(Worker.Status.IN_RECOVERY);
        when(workerNodeService.findByUuid("123")).thenReturn(mockWorker);
        workerRecoveryService.doWorkerAndMessageRecovery("123");

        //Make sure the methods did run
        verify(workerNodeService, times(1)).updateStatusInSeparateTransaction("123", Worker.Status.IN_RECOVERY);
    }

    @Test
    //Test the situation when the worker is non responsive and has not acked messages
    public void testDoWorkerAndMessageRecoveryNonResponsiveAndHasMessages() throws Exception {
        when(workerNodeService.readNonRespondingWorkers()).thenReturn(getNonResponsiveWorkers());
        when(executionQueueService.countMessagesWithoutAckForWorker(anyInt(), anyLong(), anyString())).thenReturn(200);
        WorkerNode mockWorker = mock(WorkerNode.class);
        when(mockWorker.getStatus()).thenReturn(Worker.Status.IN_RECOVERY);
        when(workerNodeService.findByUuid("123")).thenReturn(mockWorker);
        workerRecoveryService.doWorkerAndMessageRecovery("123");
        //Make sure the methods did run
        verify(workerNodeService, times(1)).updateStatusInSeparateTransaction("123", Worker.Status.IN_RECOVERY);
    }

    @Test
    public void testDoWorkerRecovery() throws Exception {
        workerRecoveryService.doWorkerRecovery("worker1");

        verify(workerLockService, times(1)).lock("worker1");
        verify(workerNodeService, times(1)).updateStatusInSeparateTransaction("worker1", Worker.Status.IN_RECOVERY);
        verify(workerNodeService, times(1)).updateStatus("worker1", Worker.Status.RECOVERED);
    }

    private List<String> getNonResponsiveWorkers() {
        List<String> workers = new ArrayList<>();
        workers.add("123");
        return workers;
    }

    @Configuration
    static class EmptyConfig {
    }
}
