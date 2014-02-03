package com.hp.oo.execution.services;

/**
 * @author Ronen Shaban
 * Date: 1/05/12
 */

import com.hp.oo.internal.sdk.execution.events.ExecutionEvent;

import java.util.ArrayList;
import java.util.List;

public class ExecutionEventLogFilterServiceTest {

	private List<ExecutionEvent> successFilterList = new ArrayList<>();
	private List<ExecutionEvent> discardFilterList = new ArrayList<>();

	public List<ExecutionEvent> getSuccessFilterList() {
		return successFilterList;
	}

	public List<ExecutionEvent> getDiscardFilterList() {
		return discardFilterList;
	}

	public void updateSuccessFilterList(ExecutionEvent event) {
		successFilterList.add(event);
	}

	public void updateDiscardFilterList(ExecutionEvent event) {
		discardFilterList.add(event);
	}



}
