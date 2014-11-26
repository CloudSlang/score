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
package org.eclipse.score.worker.management.services;

import org.eclipse.score.orchestrator.entities.Message;

/**
 * User: zruya
 * Date: 08/09/13
 * Time: 17:13
 */
public interface OutboundBuffer {
	/**
	 * Add a collection of messages to the buffer
	 * Thread safe
	 *
     * @param messages collection of elements to be added
     */
	void put(final Message... messages) throws InterruptedException;

	/**
	 * Drains the buffer
	 * this method is called in order to drain the buffer,
	 * it is not safe to call drain concurrently,
	 * it is safe to call put and drain concurrently
	 */
	void drain();

	/**
	 * @return current amount of messages in the buffer
	 */
	int getSize();

	/**
	 * @return current total weight of messages in the buffer
	 */
	int getWeight();

    /**
     * @return : the capacity of the buffer
     */
    int getCapacity();

	String getStatus();
}
