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
package io.cloudslang.score.facade.execution;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/12/12
 * Time: 11:55
 */
public enum PauseReason {
    USER_PAUSED,
    INPUT_REQUIRED,
    INPUT_REQUIRED_MANUAL_OP,
	SELECT_TRANSITION,
    DISPLAY,
    GATED_TRANSITION,
    HAND_OFF,
    INTERRUPT,
    NO_WORKERS_IN_GROUP,
    BRANCH_PAUSED,
    SEQUENTIAL_EXECUTION,
    NO_ROBOTS_IN_GROUP,
    PENDING_ROBOT,
    PRECONDITION_NOT_FULFILLED
}
