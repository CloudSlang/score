package com.hp.score.worker.management.services;

import com.hp.score.orchestrator.entities.Message;

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
	void put(Message... messages);

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
