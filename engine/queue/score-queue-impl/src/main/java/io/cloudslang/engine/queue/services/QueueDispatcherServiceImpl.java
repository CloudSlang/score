/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.Payload;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;


/**
 * User:
 * Date: 10/09/12
 * Time: 11:01
 */
public final class QueueDispatcherServiceImpl implements QueueDispatcherService {
	private final Logger logger = Logger.getLogger(getClass());

	@Autowired
	private ExecutionQueueService execQueue;

	@Transactional
	@Override
	public void dispatch(List<ExecutionMessage> messages) {
        if (messages == null || messages.isEmpty()){
            if (logger.isDebugEnabled()) logger.debug("Messages list is null or empty");
            return;
        }

		if (logger.isDebugEnabled()) logger.debug("Dispatching " + messages.size() + " messages");
		execQueue.enqueue(messages);
		if (logger.isDebugEnabled()) logger.debug("Dispatching is done");
	}

	@Transactional
	@Override
	public List<ExecutionMessage> poll(String workerId, int maxSize) {
		try {
			if (logger.isDebugEnabled()) logger.debug("Polling messages for worker [" + workerId + "], max size " + maxSize);
			// poll assigned messages to workerID
			long t = System.currentTimeMillis();
			List<ExecutionMessage> result = execQueue.poll(workerId, maxSize, ExecStatus.ASSIGNED);
			t = System.currentTimeMillis()-t;
			if (logger.isDebugEnabled()) logger.debug("Poll: " + result.size() + "/" + t + " messages/ms");

			if (!result.isEmpty()){
				t = System.currentTimeMillis();
				// change status to SENT
				for(ExecutionMessage msg:result){
					msg.setStatus(ExecStatus.SENT);
					msg.incMsgSeqId();
				}
				// update the queue
				execQueue.enqueue(result);
				t = System.currentTimeMillis()-t;
				if (logger.isDebugEnabled()) logger.debug("Enqueue: " + result.size() + "/" + t + " messages/ms");
			}
			// send the result to the worker
			if (logger.isDebugEnabled()) logger.debug("Polled " + result.size() + " messages for worker [" + workerId + ']');
			return result;
		}
		catch (Exception ex){
			//This can happen if the InBuffer retries while the first try is still running on the server side
			//The UC is preventing the duplication
			logger.error("Error while polling assigned messages for worker " + workerId, ex);
			throw ex;
		}
	}

	@Transactional
	@Override
	public void dispatch(String messageId, String group, ExecStatus status, Payload payload) {
		Validate.notEmpty(messageId, "Message ID is null or empty");
		Validate.notNull(status, "Status is null");

		group = !StringUtils.isEmpty(group)? group: WorkerNode.DEFAULT_WORKER_GROUPS[0];

		ExecutionMessage message = new ExecutionMessage(ExecutionMessage.EMPTY_EXEC_STATE_ID,
				ExecutionMessage.EMPTY_WORKER,
				group,
				messageId,
				status,
				payload,
				0);
		dispatch(Arrays.asList(message));
	}
}
