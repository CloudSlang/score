/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.score.events;

import java.io.Serializable;

/**
 * User:
 * Date: 10/06/2014
 */
public class ScoreEvent implements Serializable {

	private String eventType;
	private Serializable data;

	public ScoreEvent(String eventType, Serializable data) {
		this.eventType = eventType;
		this.data = data;
	}

	public String getEventType() {
		return eventType;
	}

	public Serializable getData() {
		return data;
	}
}
