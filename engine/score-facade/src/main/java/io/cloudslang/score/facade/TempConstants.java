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

package io.cloudslang.score.facade;

/**
 * Created by peerme on 03/09/2014.
 */
public class TempConstants {

    // the actually entry in the queue that the worker should run. like the name of the worker itself that
    // was chosen from the group
    public static final String ACTUALLY_OPERATION_GROUP = "ACTUALLY_OPERATION_GROUP";

    public static final String DEFAULT_GROUP = "RAS_Operator_Path";

    // For external workers optimization in PreOp
    public static final String CONTENT_EXECUTION_STEP = "content_step";

    //This flag is set if the current execution step is important and must be persisted
    public static final String IS_RECOVERY_CHECKPOINT = "IS_RECOVERY_CHECKPOINT";

    // This flag is set if the current execution step needs to go through group resolving
    public static final String SHOULD_CHECK_GROUP = "SHOULD_CHECK_GROUP";

    // For the studio debugger events
    public static final String DEBUGGER_MODE = "DEBUGGER_MODE";

    public static final String USE_DEFAULT_GROUP = "USE_DEFAULT_GROUP";

    public static final String SC_TIMEOUT_START_TIME = "SC_TIMEOUT_START_TIME";
    public static final String SC_TIMEOUT_MINS = "SC_TIMEOUT_MINS";

    public static final String EXECUTE_CONTENT_ACTION_CLASSNAME = "ContentExecutionActions";
    public static final String EXECUTE_CONTENT_ACTION = "executeContentAction";

}
