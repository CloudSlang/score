/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
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
