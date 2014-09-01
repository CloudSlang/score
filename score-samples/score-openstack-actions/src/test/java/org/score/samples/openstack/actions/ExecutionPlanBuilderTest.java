package org.score.samples.openstack.actions;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.lang.ExecutionRuntimeServices;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hp.score.events.EventConstants.SCORE_FINISHED_EVENT;
import static org.junit.Assert.assertEquals;
import static org.score.samples.openstack.actions.FinalStepActions.RESPONSE_KEY;
import static org.score.samples.openstack.actions.FinalStepActions.SUCCESS_KEY;

public class ExecutionPlanBuilderTest {
	public static final String FINAL_STEP_ACTIONS_CLASS = "org.score.samples.openstack.actions.FinalStepActions";
	private final static Logger logger = Logger.getLogger(ExecutionPlanBuilderTest.class);
	public static final String ECHO_CLASS = "org.score.samples.openstack.actions.ExecutionPlanBuilderTest";
	public static final String ECHO_ACTION = "echo";
	public static final String SUCCESS_STEP_ACTION = "successStepAction";
	public static final String FAILURE_STEP_ACTION = "failureStepAction";
	public static final String MESSAGE_KEY = "message";
	public static final String MESSAGE_SUBFLOW = "MESSAGE";
	public static final String MESSAGE_PARENT = "NO_OVERRIDE";
	private static final String ECHO_EVENT = "echo event";
	private List<ScoreEvent> eventList;

	private static final long DEFAULT_TIMEOUT = 60000;

	@Autowired
	private Score score;

	@Autowired
	private EventBus eventBus;

	@Before
	public void init(){
		eventList = Collections.synchronizedList(new ArrayList<ScoreEvent>());
	}

	@Test(timeout = DEFAULT_TIMEOUT) //TODO - refactor test / solve timeout exception
	public void testSubflow() throws Exception {
		ExecutionPlanBuilderTest app = loadApp();

		ExecutionPlan subFlow = createSubFlow();
		TriggeringProperties parentFlowProperties = createParentFlow(subFlow);

		registerEventListeners(app);

		app.score.trigger(parentFlowProperties);

		waitForScoreToFinish();

		assertEquals("2 echo events should have been fired", 2, filterEventsQueue(eventList, ECHO_EVENT).size());
	}

	private ExecutionPlan createSubFlow() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

		//echo step
		builder.addStep(0L, ECHO_CLASS, ECHO_ACTION, 1L);

		//success step
		builder.addOOActionFinalStep(1L, FINAL_STEP_ACTIONS_CLASS, SUCCESS_STEP_ACTION);

		return builder.createTriggeringProperties().getExecutionPlan();
	}

	private TriggeringProperties createParentFlow(ExecutionPlan subFlow) {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
		builder.setBeginStep(0L);

		Map<String, Serializable> flowInputs = new HashMap<>();
		flowInputs.put(MESSAGE_KEY, MESSAGE_PARENT);
		builder.setInitialExecutionContext(flowInputs);

		//add subflow
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, 2L)); //success
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 4L)); //failure
		Map<String, Serializable> subflowInputs = new HashMap<>();
		subflowInputs.put(MESSAGE_KEY, MESSAGE_SUBFLOW);
		TriggeringProperties triggeringProperties = TriggeringProperties.create(subFlow);
		triggeringProperties.setContext(subflowInputs);
		builder.addSubflow(0L, 1L, triggeringProperties, null, navigationMatchers);

		//echo step
		builder.addStep(2L, ECHO_CLASS, ECHO_ACTION, 3L);

		//success step
		builder.addOOActionFinalStep(3L, FINAL_STEP_ACTIONS_CLASS, SUCCESS_STEP_ACTION);

		//failure step
		builder.addOOActionFinalStep(4L, FINAL_STEP_ACTIONS_CLASS, FAILURE_STEP_ACTION);

		return builder.createTriggeringProperties();
	}

	private static ExecutionPlanBuilderTest loadApp() {
		ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/executionPlanBuilderTestContext.xml");
		ExecutionPlanBuilderTest app;
		app = context.getBean(ExecutionPlanBuilderTest.class);
		return app;
	}

	private void waitForScoreToFinish() {
		while(filterEventsQueue(eventList, SCORE_FINISHED_EVENT).size() < 1){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private List<ScoreEvent> filterEventsQueue(List<ScoreEvent> eventQueue, String eventType) {
		List<ScoreEvent> returnList = new ArrayList<>();
		for (ScoreEvent event : eventQueue) {
			if (event.getEventType().equals(eventType)) {
				returnList.add(event);
			}
		}
		return returnList;
	}

	@SuppressWarnings("unused")
	public Map<String, String> echo(String message, ExecutionRuntimeServices executionRuntimeServices) {
		if (message == null) {
			message = "DEFAULT";
		}
		logger.info("ECHO action invoked - " + message);
		executionRuntimeServices.addEvent(ECHO_EVENT, ECHO_EVENT);
		return new HashMap<>();
	}

	private void registerEventListeners(ExecutionPlanBuilderTest app) {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(OOActionRunner.ACTION_RUNTIME_EVENT_TYPE);
		handlerTypes.add(ECHO_EVENT);
		registerInfoEventListener(app, handlerTypes);

		//register listener for action exception events
		handlerTypes = new HashSet<>();
		handlerTypes.add(OOActionRunner.ACTION_EXCEPTION_EVENT_TYPE);
		registerExceptionEventListener(app, handlerTypes);

		registerScoreEventListener(app);
	}

	private void registerExceptionEventListener(ExecutionPlanBuilderTest app, Set<String> handlerTypes) {
		app.eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				eventList.add(event);
				logExceptionListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void registerInfoEventListener(ExecutionPlanBuilderTest app, Set<String> handlerTypes) {
		app.eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				eventList.add(event);
				logListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void registerScoreEventListener(ExecutionPlanBuilderTest app) {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
		handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
		handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
		app.eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				if(event.getEventType().equals(EventConstants.SCORE_FINISHED_EVENT)){   //TODO - temp solution, till only end flow events send SCORE_FINISHED_EVENT (now also branch throw this event)
					@SuppressWarnings("all")
					Map<String,Serializable> data = (Map<String,Serializable>)event.getData();
					if ((Boolean)data.get(EventConstants.IS_BRANCH)) {
						return;
					}
				}
				eventList.add(event);
				logScoreListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void logExceptionListenerEvent(ScoreEvent event) {
		logger.info("Event " + event.getEventType() + " occurred: " + event.getData());
		Exception exception = (Exception) event.getData();
		exception.printStackTrace();
	}

	private void logListenerEvent(ScoreEvent event) {
		logger.info("Event " + event.getEventType() + " occurred: " + event.getData());
	}

	private void logScoreListenerEvent(ScoreEvent event) {
		logger.info("Event " + event.getEventType() + " occurred");
	}
}