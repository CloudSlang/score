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
    private String languageName;
	private Serializable data;

	public ScoreEvent(String eventType, Serializable data) {
		this.eventType = eventType;
		this.data = data;
	}

    public ScoreEvent(String eventType, String languageName, Serializable data) {
        this(eventType,data);
        this.languageName = languageName;
    }

    public String getEventType() {
		return eventType;
	}

    public String getLanguageName() {
        return languageName;
    }

    public Serializable getData() {
		return data;
	}
}
