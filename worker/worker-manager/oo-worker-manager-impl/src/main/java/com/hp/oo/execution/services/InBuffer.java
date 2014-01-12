package com.hp.oo.execution.services;

import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.services.QueueDispatcherService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/11/12
 * Time: 08:46
 */
@Component
public class InBuffer implements ApplicationListener, Runnable, WorkerRecoveryListener {

    private final long MEMORY_THRESHOLD = 50000000; // 50 Mega byte

	private final Logger logger = Logger.getLogger(this.getClass());

	@Autowired
	private QueueDispatcherService queueDispatcher;

	@Resource
	private String workerUuid;

	@Autowired
	@Qualifier("inBufferCapacity")
	private Integer capacity;

	@Autowired(required = false)
	@Qualifier("coolDownPollingMillis")
	private Integer coolDownPollingMillis = 300;

	private Thread fillMsgsThread = new Thread(this);

	private boolean inShutdown;

	private boolean endOfInit = false;

	@Autowired
	private WorkerManager workerManager;

	@Autowired
	private SimpleExecutionRunnableFactory simpleExecutionRunnableFactory;

    @Autowired
    private OutboundBuffer outBuffer;

	@Autowired
	private WorkerRecoveryManagerImpl recoveryManager;

	private AtomicBoolean recoveryFlag = new AtomicBoolean(false);

    private Date currentCreateDate = new Date(0);

    private void fillBufferPeriodically() {
        long pollCounter = 0;
        while (!inShutdown) {
            pollCounter = pollCounter+ 1;
            // we reset the currentCreateDate every 100 queries , for the theoretical problem of records
            // with wrong order of create_time in the queue table.
            if ((pollCounter % 100) == 0) {
                currentCreateDate = new Date(0);
            }
            try {
                boolean workerUp = workerManager.isUp();
                if(!workerUp) {
                    Thread.sleep(3000); //sleep if worker is not fully started yet
                }
                else {
                    //double check if we still need to fill the buffer - we are running multi threaded
                    int bufferSize = workerManager.getInBufferSize();
                    if (logger.isDebugEnabled()) logger.debug("InBuffer size: " + bufferSize);

                    if (!recoveryManager.isInRecovery() && bufferSize < (capacity * 0.2) && checkFreeMemorySpace(MEMORY_THRESHOLD)) {
                        int messagesToGet = capacity - workerManager.getInBufferSize();

                        if (logger.isDebugEnabled()) logger.debug("Polling messages from queue (max " + messagesToGet + ")");
                        List<ExecutionMessage> newMessages = queueDispatcher.poll(workerUuid, messagesToGet, currentCreateDate);
                        if (logger.isDebugEnabled()) logger.debug("Received " + newMessages.size() + " messages from queue");

                        if (!newMessages.isEmpty()) {
                            // update currentCreateDate;
                            currentCreateDate = new Date(newMessages.get(newMessages.size()-1).getCreateDate().getTime() - 100);

                            //we must acknowledge the messages that we took from the queue
                            ackMessages(newMessages);
                            for(ExecutionMessage msg :newMessages){
                                addExecutionMessage(msg);
                            }
							Thread.sleep(coolDownPollingMillis/8); //if there are no messages - sleep a while
                        } else {
                            Thread.sleep(coolDownPollingMillis); //if there are no messages - sleep a while
                        }
                    }
                    else {
	                    if (recoveryManager.isInRecovery() && logger.isDebugEnabled()) logger.debug("in buffer waits for recovery ...");
	                    Thread.sleep(coolDownPollingMillis); //if the buffer is not empty enough yet - sleep a while
                    }
                }
            } catch (Exception ex) {
                logger.error("Failed to load new ExecutionMessages to the buffer!", ex);
                try {Thread.sleep(1000);} catch (InterruptedException e) {/*ignore*/}
            }
        }
    }

    private void ackMessages(List<ExecutionMessage> newMessages) {
        ExecutionMessage cloned;
        for (ExecutionMessage message : newMessages) {
	        // create a unique id for this lane in this specific worker to be used in out buffer optimization
	        message.setWorkerKey(message.getMsgId() + " : " + message.getExecStateId());
            cloned = (ExecutionMessage) message.clone();
            cloned.setStatus(ExecStatus.IN_PROGRESS);
            cloned.incMsgSeqId();
            message.incMsgSeqId(); // increment the original message seq too in order to preserve the order of all messages of entire step
            cloned.setPayload(null); //payload is not needed in ack - make it null in order to minimize the data that is being sent
	        outBuffer.put(cloned);
        }
    }


    public void addExecutionMessage(ExecutionMessage msg) {
        SimpleExecutionRunnable simpleExecutionRunnable = simpleExecutionRunnableFactory.getObject();
        simpleExecutionRunnable.setExecutionMessage(msg);
        simpleExecutionRunnable.setRecoveryFlag(recoveryFlag);
        workerManager.addExecution(msg.getMsgId(), simpleExecutionRunnable);
    }

	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		if (applicationEvent instanceof ContextRefreshedEvent && ! endOfInit) {
			endOfInit = true;
			inShutdown = false;
			fillMsgsThread.setName("WorkerFillBufferThread");
			fillMsgsThread.start();
		} else if (applicationEvent instanceof ContextClosedEvent) {
			inShutdown = true;
		}
	}

	@Override
	public void run() {
		fillBufferPeriodically();
	}

	@Override
	public void doRecovery() {
		if (logger.isDebugEnabled()) logger.debug("Begin in buffer recovery");
		recoveryFlag.set(true);
		recoveryFlag = new AtomicBoolean(false);
		if (logger.isDebugEnabled()) logger.debug("In buffer recovery is done");
	}


    public boolean checkFreeMemorySpace(long threshold){
        double allocatedMemory      = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        double presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
        boolean result = presumableFreeMemory > threshold;
        if (! result) {
            logger.warn("InBuffer would not poll messages, because there is not enough free memory.");
        }
        return result;
    }
}
