/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import org.openscore.events.EventBus;
import org.openscore.events.ScoreEvent;
import org.openscore.facade.entities.Execution;
import org.openscore.facade.execution.ExecutionSummary;
import org.openscore.lang.SystemContext;
import org.openscore.orchestrator.services.ExecutionStateService;
import org.openscore.orchestrator.services.PauseResumeService;
import org.openscore.orchestrator.services.SplitJoinService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User:
 * Date: 20/07/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class QueueListenerImplTest {

	@Autowired
	private QueueListener queueListener;

	@Autowired
	private EventBus eventBus;

	@Autowired
	private ExecutionMessageConverter executionMessageConverter;

	@Autowired
	private ScoreEventFactory scoreEventFactory;

	@Autowired
	private ExecutionStateService executionStateService;

	@Autowired
	private SplitJoinService splitJoinService;

	@Before
	public void setup() throws IOException {
		reset(eventBus);
	}

	@Test
	public void testOnTerminatedWhenNoMessages() throws InterruptedException {
		List<ExecutionMessage> messages = new ArrayList<>();
		queueListener.onTerminated(messages);

		verify(eventBus, never()).dispatch();
	}

	@Test
	public void testEventsThrownForOnTerminated() throws InterruptedException {
		List<ExecutionMessage> messages = new ArrayList<>();
		messages.add(createExecutionMessage());
		messages.add(createExecutionMessage());

		ScoreEvent event1 = new ScoreEvent("type1", "event1");
		ScoreEvent event2 = new ScoreEvent("type1", "event2");
		when(scoreEventFactory.createFinishedEvent(any(Execution.class))).thenReturn(event1, event2);
		queueListener.onTerminated(messages);

		verify(eventBus, times(1)).dispatch(event1, event2);
	}

	@Test
	public void testOnTerminatedNonBranchExecution() {
		List<ExecutionMessage> messages = new ArrayList<>();
		messages.add(createExecutionMessage());
		messages.add(createExecutionMessage());

		ScoreEvent event1 = new ScoreEvent("type1", "event1");
		ScoreEvent event2 = new ScoreEvent("type1", "event2");
		when(scoreEventFactory.createFinishedEvent(any(Execution.class))).thenReturn(event1, event2);
		queueListener.onTerminated(messages);

		verify(executionStateService, times(1)).deleteExecutionState(Long.valueOf(messages.get(0).getMsgId()), ExecutionSummary.EMPTY_BRANCH);
		verify(executionStateService, times(1)).deleteExecutionState(Long.valueOf(messages.get(1).getMsgId()), ExecutionSummary.EMPTY_BRANCH);
	}

	@Test
	public void testOnTerminatedBranchExecution() {
		List<ExecutionMessage> messages = new ArrayList<>();
		Execution execution1 = createBranchExecution();
		Execution execution2 = createBranchExecution();
		messages.add(createExecutionMessage(execution1));
		messages.add(createExecutionMessage(execution2));

		queueListener.onTerminated(messages);

		verify(splitJoinService, times(1)).endBranch((List<Execution>) argThat(hasItem(execution1)));
		verify(splitJoinService, times(1)).endBranch((List<Execution>) argThat(hasItem(execution2)));
	}

	private Execution createBranchExecution() {
		SystemContext systemContext = new SystemContext();
		systemContext.setBranchId(UUID.randomUUID()
                                      .toString());
		Random random = new Random();
		return new Execution(random.nextLong(), random.nextLong(), 0L, new HashMap<String, String>(0), systemContext);
	}

	private ExecutionMessage createExecutionMessage() {
		return createExecutionMessage(new Execution());
	}

	private ExecutionMessage createExecutionMessage(Execution execution) {
		ExecutionMessage executionMessage = new ExecutionMessage();
		executionMessage.setMsgId(String.valueOf(new Random().nextLong()));
		executionMessage.setPayload(executionMessageConverter.createPayload(execution));
		return executionMessage;
	}

	@Test
	public void testOnFailedWhenNoMessages() throws InterruptedException {
		List<ExecutionMessage> messages = new ArrayList<>();
		queueListener.onFailed(messages);
		verify(eventBus, never()).dispatch();
	}

	@Test
	public void testOnFailedSendsEvents() throws InterruptedException {
		List<ExecutionMessage> messages = new ArrayList<>();
		Execution execution1 = createBranchExecution();
		Execution execution2 = new Execution();
		messages.add(createExecutionMessage(execution1));
		messages.add(createExecutionMessage(execution2));

		ScoreEvent event1 = new ScoreEvent("type1", "event1");
		ScoreEvent event2 = new ScoreEvent("type1", "event2");
		when(scoreEventFactory.createFailedBranchEvent(execution1)).thenReturn(event1);
		when(scoreEventFactory.createFailureEvent(execution2)).thenReturn(event2);
		queueListener.onFailed(messages);

		verify(eventBus, times(1)).dispatch(event1, event2);
	}

	@Test
	public void testOnFailedDeletesExecutionStates() {
		List<ExecutionMessage> messages = new ArrayList<>();
		messages.add(createExecutionMessage());
		messages.add(createExecutionMessage());

		queueListener.onFailed(messages);
		verify(executionStateService, times(1)).deleteExecutionState(Long.valueOf(messages.get(0).getMsgId()), ExecutionSummary.EMPTY_BRANCH);
		verify(executionStateService, times(1)).deleteExecutionState(Long.valueOf(messages.get(1).getMsgId()), ExecutionSummary.EMPTY_BRANCH);
	}

	@Test
	public void testOnFailedBranchExecution() {
		List<ExecutionMessage> messages = new ArrayList<>();
		Execution execution1 = createBranchExecution();
		Execution execution2 = createBranchExecution();
		messages.add(createExecutionMessage(execution1));
		messages.add(createExecutionMessage(execution2));

		queueListener.onFailed(messages);

		verify(splitJoinService, times(1)).endBranch((List<Execution>) argThat(hasItem(execution1)));
		verify(splitJoinService, times(1)).endBranch((List<Execution>) argThat(hasItem(execution2)));
	}

	@Configuration
	static class QueueListenerImplTestContext {

		@Bean
		QueueListener queueListener() {
			return new QueueListenerImpl();
		}

		@Bean
		ExecutionStateService executionStateService() {
			return mock(ExecutionStateService.class);
		}

		@Bean
		ExecutionMessageConverter executionMessageConverter() {
			return new ExecutionMessageConverter();
		}

		@Bean
		EventBus eventBus() {
			return mock(EventBus.class);
		}

		@Bean
		SplitJoinService splitJoinService() {
			return mock(SplitJoinService.class);
		}

		@Bean
		ScoreEventFactory scoreEventFactory() {
			return mock(ScoreEventFactory.class);
		}

		@Bean
		PauseResumeService pauseResumeService() {
			return mock(PauseResumeService.class);
		}
	}

}
