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

package io.cloudslang.engine.queue.entities;

import org.apache.commons.lang.Validate;

import java.util.Collections;
import java.util.List;

/**
 * User:
 * Date: 11/26/12
 */
public class ExecutionMessageList {
	private List<ExecutionMessage> list = Collections.emptyList();

	@SuppressWarnings("unused")
	private ExecutionMessageList(){/*used by JSON*/}

	public ExecutionMessageList(List<ExecutionMessage> list){
		Validate.notNull(list, "A list is null");
		this.list = list;
	}

	public List<ExecutionMessage> getList() {
		return list;
	}
}
