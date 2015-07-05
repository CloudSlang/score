/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.orchestrator.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 12/3/13
 *
 *
 * An in interface for messages in the execution buffers
 */
public interface Message extends Serializable {
    /**
     *
     * return the id of the message
     *
     * @return the id of the message
     */
	String getId();

    /**
     *
     * return the weight of the message
     * used for thresholds in the buffers
     *
     * @return the weight of the message
     */
	int getWeight();

    /**
     *
     * shrinks a list of {@link Message} if possible
     *
     * @param messages list of {@link Message} to shrink
     * @return list of {@link Message} after shrinking
     */
	List<Message> shrink(List<Message> messages);

    String getExceptionMessage();

    void setExceptionMessage(String msg);
}
