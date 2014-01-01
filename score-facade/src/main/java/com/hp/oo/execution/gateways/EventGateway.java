package com.hp.oo.execution.gateways;

import com.hp.oo.internal.sdk.execution.events.ExecutionEvent;

/**
 *
 * @author Ronen Shaban
 */
public interface EventGateway {
	void addEvent(ExecutionEvent executionEvent);
}
