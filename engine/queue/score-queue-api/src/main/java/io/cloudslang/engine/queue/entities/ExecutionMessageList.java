/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

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
