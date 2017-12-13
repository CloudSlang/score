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

/**
 * Created with IntelliJ IDEA.
 * User:
 * Date: 09/01/14
 * To change this template use File | Settings | File Templates.
 */
public interface ScoreEventListener {

    /**
     * handler of score event, this method will be called on score event
     * @param event - the event that dispatched
     */
	void onEvent(ScoreEvent event) throws InterruptedException ;

}
