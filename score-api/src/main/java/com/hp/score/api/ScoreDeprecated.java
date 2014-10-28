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
package com.hp.score.api;

/**
 * Created by peerme on 23/07/2014.
 */
@Deprecated
public interface ScoreDeprecated {

    /***
     * for cases you need the executionId before triggering
     * this method generate executionId
     * @return  the executionId generated
     */
    public Long generateExecutionId();

    /**
     * for cases you need the executionId before triggering
     * trigger run with pre-generated executionId (by using generateExecutionId() method...)
     * @param executionId  - the executionId for the run
     * @param triggeringProperties   object holding all the properties needed for the trigger
     * @return the give executionId
     */
    public Long trigger(Long executionId, TriggeringProperties triggeringProperties);
}
