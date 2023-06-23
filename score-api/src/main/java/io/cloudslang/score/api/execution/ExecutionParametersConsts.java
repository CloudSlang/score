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
package io.cloudslang.score.api.execution;

/**
 * Created by peerme on 03/09/2014.
 */
public class ExecutionParametersConsts {

	public static final String EXECUTION = "execution";
	public static final String ACTION_TYPE = "actionType";
	public static final String EXECUTION_CONTEXT = "executionContext";
	public static final String SYSTEM_CONTEXT = "systemContext";

	public static final String GLOBAL_SESSION_OBJECT = "globalSessionObject";
	public static final String SESSION_OBJECT = "sessionObject";

	public static final String EXECUTION_RUNTIME_SERVICES = "executionRuntimeServices";
	public static final String SERIALIZABLE_SESSION_CONTEXT = "serializableSessionContext"; // sits in PluginParams
	public static final String NON_SERIALIZABLE_EXECUTION_DATA = "nonSerializableExecutionData";
	public static final String RUNNING_EXECUTION_PLAN_ID = "RUNNING_EXECUTION_PLAN_ID";
	public static final String FINISHED_CHILD_BRANCHES_DATA = "FINISHED_CHILD_BRANCHES_DATA";
	public static final String SEQUENTIAL = "sequential";

	public static final double DEFAULT_ROI_VALUE = 0.0;
	public static final String EXECUTION_TOTAL_ROI = "execution_total_roi";
}
