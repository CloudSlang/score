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

package io.cloudslang.score.events;

import java.io.Serializable;
import java.util.Map;

/**
 * User:
 * Date: 10/06/2014
 */
public class ScoreEvent implements Serializable {

	private String eventType;
    private String languageName;
	private Serializable data;
    private Map<String, ? extends  Serializable> metadata;

	public ScoreEvent(String eventType, Serializable data) {
		this.eventType = eventType;
		this.data = data;
	}

    public ScoreEvent(String eventType, String languageName, Serializable data) {
        this(eventType,data);
        this.languageName = languageName;
    }

    public ScoreEvent(String eventType, String languageName, Serializable data, Map<String, ? extends  Serializable> metadata) {
        this.eventType = eventType;
        this.languageName = languageName;
        this.data = data;
        this.metadata = metadata;
    }

    public Map<String, ? extends  Serializable> getMetadata() {
        return metadata;
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
