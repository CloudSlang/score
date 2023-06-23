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
package io.cloudslang.orchestrator.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 12/3/13
 *
 *
 * An in interface for messages in the execution buffers
 */
public interface Message extends Serializable{
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
}
