/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.engine.queue.services;

import org.eclipse.score.engine.queue.entities.ExecStatus;
import org.eclipse.score.engine.queue.entities.ExecutionMessage;
import org.eclipse.score.engine.queue.entities.Payload;

import java.util.Date;
import java.util.List;

/**
 * User:
 * Date: 10/09/12
 * Time: 11:00
 */
//TODO: Add Javadoc
public interface QueueDispatcherService {

	void dispatch(List<ExecutionMessage> messages);

	List<ExecutionMessage> poll(String uuid, int maxSize, Date createDate);

	void dispatch(String messageId, String group, ExecStatus status, Payload payload);
}
