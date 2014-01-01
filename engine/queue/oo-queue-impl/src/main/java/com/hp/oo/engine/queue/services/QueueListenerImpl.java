package com.hp.oo.engine.queue.services;

import com.hp.oo.engine.queue.entities.ExecutionMessage;
import org.apache.log4j.Logger;
import java.util.List;

/**
 * User: Amit Levin
 * Date: 19/09/12
 * Time: 15:14
 */
@SuppressWarnings("unused")
public class QueueListenerImpl implements  QueueListener {

	private static Logger logger = Logger.getLogger(QueueListenerImpl.class);

	@Override
	public void onEnqueue(List<ExecutionMessage> messages,int queueSize) {
		if (logger.isDebugEnabled()){
			logger.debug("Enqueue " + messages.size() + " messages:");
			logger.debug("queue size: " + queueSize);
			if (logger.isTraceEnabled()) {
				for(ExecutionMessage msg:messages){
					logger.trace("Enqueue msgId= " + msg.getMsgUniqueId()+":"+ msg.getMsgSeqId()+",workerId=" + msg.getWorkerId()+",status="+msg.getStatus());
				}
			}
		}
	}

	@Override
	public void onPoll(List<ExecutionMessage> messages,int queueSize) {
		if (logger.isDebugEnabled()){
			logger.debug("poll " + messages.size() + " messages:");
			logger.debug("queue size: " + queueSize);
		    if (logger.isTraceEnabled()) {
				for(ExecutionMessage msg:messages){
					logger.trace("Poll msgId= " + msg.getMsgUniqueId()+":"+ msg.getMsgSeqId()+",workerId=" + msg.getWorkerId()+",status="+msg.getStatus());
				}
		    }
		}
	}

	@Override
	public void onTerminated(List<ExecutionMessage> messages) {
	}

	@Override
	public void onFailed(List<ExecutionMessage> messages) {
	}

}
