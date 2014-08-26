package com.hp.score.engine.queue.entities;

import org.apache.commons.lang.Validate;

import java.util.Collections;
import java.util.List;

/**
 * User: Dima Rassin
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
