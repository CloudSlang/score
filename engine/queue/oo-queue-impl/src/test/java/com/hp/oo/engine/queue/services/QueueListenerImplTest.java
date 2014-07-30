package com.hp.oo.engine.queue.services;

import com.hp.oo.engine.queue.entities.ExecutionMessage;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.entities.Payload;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.orchestrator.services.SplitJoinService;
import com.hp.score.events.EventBus;
import com.hp.score.services.ExecutionStateService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: maromg
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

	@Before
	public void setup() throws IOException {
		reset(eventBus);
		when(executionMessageConverter.extractExecution(any(Payload.class))).thenReturn(createExecution());
	}

	@Test
	public void testOnTerminatedWhenNoMessages() {
		List<ExecutionMessage> messages = new ArrayList<>();
		queueListener.onTerminated(messages);

		verify(eventBus, never()).dispatch();
	}

	@Test
	public void testOnFailedWhenNoMessages() {
		List<ExecutionMessage> messages = new ArrayList<>();
		queueListener.onFailed(messages);
		verify(eventBus, never()).dispatch();
	}

	private Execution createExecution() {
		return new Execution(
				new Random().nextLong(),
				new Random().nextLong(),
				0L,
				new HashMap<String, String>(),
				new HashMap<String, Serializable>());
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
			return mock(ExecutionMessageConverter.class);
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
	}

}
