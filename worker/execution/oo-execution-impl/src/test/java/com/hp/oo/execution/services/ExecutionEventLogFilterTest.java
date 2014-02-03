package com.hp.oo.execution.services;

/**
 * @author Ronen Shaban
 * Date: 1/05/12
 */

import com.hp.oo.enginefacade.execution.ExecutionEnums.LogLevel;
import com.hp.oo.enginefacade.execution.ExecutionEnums.LogLevelCategory;
import com.hp.oo.execution.ExecutionLogLevelHolder;
import com.hp.oo.execution.gateways.EventGateway;
import com.hp.oo.internal.sdk.execution.events.ExecutionEvent;
import com.hp.oo.internal.sdk.execution.events.ExecutionEventFactory;
import com.hp.oo.internal.sdk.execution.events.ExecutionEventUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/spring/executionEventFilterContext.xml")
public class ExecutionEventLogFilterTest {

	@Autowired
	private EventGateway gateWay;

	@Autowired
	private ExecutionEventLogFilterServiceTest executionEventLogFilterServiceTest;

    private Map<String, Serializable> systemContext;

    @Before
    public void setUp() {
        systemContext = new HashMap<>();
        ExecutionEventUtils.startFlow(systemContext);
    }


	@Test(timeout = 180000)
	public void testExecutionEventFilter() {
		// set the default log level
		ExecutionLogLevelHolder.setExecutionLogLevel(LogLevel.INFO);
		String executionId = UUID.randomUUID().toString();

		List<ExecutionEvent> successFilterEvents = createSuccessFilterEvents(executionId);
		for (ExecutionEvent event : successFilterEvents) {
			gateWay.addEvent(event);
		}
		assertEquals(successFilterEvents.size(), executionEventLogFilterServiceTest.getSuccessFilterList().size());

		List<ExecutionEvent> filterDiscardEvents = createDiscardFilterEvents(executionId);
		for (ExecutionEvent event : filterDiscardEvents) {
			gateWay.addEvent(event);
		}
		assertEquals(filterDiscardEvents.size(), executionEventLogFilterServiceTest.getDiscardFilterList().size());
	}

	
	private List<ExecutionEvent> createSuccessFilterEvents(String executionId) {
		return Arrays.asList(
				ExecutionEventFactory.createStartEvent(executionId, "my flow", "manually", null, null, ExecutionEventUtils.increaseEvent(systemContext), systemContext),
				ExecutionEventFactory.createLogEvent(executionId,"step2", "test log message 2", LogLevel.INFO, LogLevelCategory.STEP_TRANSITION ,  null, ExecutionEventUtils.increaseEvent(systemContext), systemContext),
				ExecutionEventFactory.createLogEvent(executionId,"step2", "test log message 3", LogLevel.ERROR,LogLevelCategory.STEP_TRANSITION,  null, ExecutionEventUtils.increaseEvent(systemContext), systemContext),
				ExecutionEventFactory.createCompletedFinishEvent(executionId, "my flow", "content", ExecutionEventUtils.increaseEvent(systemContext), systemContext)
		);
	}

	private List<ExecutionEvent> createDiscardFilterEvents(String executionId) {
		return Arrays.asList(
				ExecutionEventFactory.createLogEvent(executionId,"step2", "test log message 1", LogLevel.DEBUG, LogLevelCategory.STEP_TRANSITION, null, ExecutionEventUtils.increaseEvent(systemContext), systemContext)
		);
	}

}
