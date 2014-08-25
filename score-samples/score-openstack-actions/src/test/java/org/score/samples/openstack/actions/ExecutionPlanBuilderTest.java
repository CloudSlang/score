package org.score.samples.openstack.actions;

import com.google.common.collect.Sets;
import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
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
import org.score.samples.controlactions.BranchActions;
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

import static org.score.samples.openstack.actions.FinalStepActions.*;
import static com.hp.score.events.EventConstants.*;
import static org.junit.Assert.assertEquals;

public class ExecutionPlanBuilderTest {
	public static final String FINAL_STEP_ACTIONS_CLASS = "org.score.samples.openstack.actions.FinalStepActions";
	private final static Logger logger = Logger.getLogger(ExecutionPlanBuilderTest.class);
	public static final String ECHO_ACTION = "echo";
	public static final String SUCCESS_STEP_ACTION = "successStepAction";
	public static final String FAILURE_STEP_ACTION = "failureStepAction";
	public static final String ECHO_CLASS = "org.score.samples.openstack.actions.ExecutionPlanBuilderTest";
	public static final String MESSAGE_KEY = "message";
	public static final String MESSAGE_SUBFLOW = "MESSAGE";
	public static final String MESSAGE_PARENT = "NO_OVERRIDE";
	private static final String ECHO_EVENT = "echo event";
	private List<ScoreEvent> eventList = Collections.synchronizedList(new ArrayList<ScoreEvent>());

	private static final long DEFAULT_TIMEOUT = 600000;

	private final static String simpleNavigationMethodName = "simpleNavigation";
	private final static String navigationActionClassName = "org.score.samples.controlactions.NavigationActions";

	@Autowired
	private Score score;

	@Autowired
	private EventBus eventBus;

	@Before
	public void init(){
		eventList = new ArrayList<>();
	}

	@Test (timeout = DEFAULT_TIMEOUT)
	public void testSubflow() throws Exception {
		ExecutionPlanBuilderTest app = loadApp();

		ExecutionPlan subFlow = createSubFlow();
		TriggeringProperties parentFlowProperties = createParentFlow(subFlow);

		registerEventListeners(app);

		app.score.trigger(parentFlowProperties);

		waitForScoreToFinish();

		assertEquals("2 echo events should have been fired", 2, filterEventsQueue(eventList, ECHO_EVENT).size());
	}

	//@Test (timeout = DEFAULT_TIMEOUT) //TODO - refactor this test
	public void testMultipleLevelSubflows() throws Exception {
		ExecutionPlanBuilderTest app = loadApp();

		ExecutionPlan subflowLevel1 = createSubflowLevel1();
		ExecutionPlan subflowLevel2 = createSubflowLevel2(subflowLevel1);
		ExecutionPlan subflowLevel3_1 = createSubflowLevel1();
		ExecutionPlan subflowLevel3 = createSubflowLevel3(subflowLevel2, subflowLevel3_1);

		subflowLevel2.setSubflowsUUIDs(Sets.newHashSet(subflowLevel1.getFlowUuid()));
		Set<String> subflows = new HashSet<>();
		subflows.add(subflowLevel2.getFlowUuid());
		subflows.add(subflowLevel3_1.getFlowUuid());
		subflowLevel3.setSubflowsUUIDs(subflows);

		TriggeringProperties triggeringProperties = TriggeringProperties.create(subflowLevel3);

		Map<String, ExecutionPlan> dependencies = new HashMap<>();
		dependencies.put(subflowLevel1.getFlowUuid(),subflowLevel1);
		dependencies.put(subflowLevel2.getFlowUuid(),subflowLevel2);
		dependencies.put(subflowLevel3_1.getFlowUuid(),subflowLevel3_1);
		triggeringProperties.setDependencies(dependencies);

		Map<String,Serializable> getRuntimeValues = new HashMap<>();
		getRuntimeValues.put("NEW_BRANCH_MECHANISM", Boolean.TRUE);//TODO - remove this !! needs to work with this on by default, pending Non-Blocking story
		triggeringProperties.setRuntimeValues(getRuntimeValues);

		Set<String> handlers = new HashSet<>();
		handlers.add("Hello score");
		registerInfoEventListener(app, handlers);

		app.score.trigger(triggeringProperties);

		waitForScoreToFinishMultipleLevels();

		assertEquals("2 hello score events should have been fired", 2, filterEventsQueue(eventList, "Hello score").size());
	}

	private ExecutionPlan createSubflowLevel3(ExecutionPlan subFlowLevel2, ExecutionPlan subFlowLevel3_1) {
		ExecutionPlan executionPlan = new ExecutionPlan();

		executionPlan.setFlowUuid("subFlowLevel3");

		executionPlan.setBeginStep(0L);

		//subflow1
		Map<String, Serializable> actionData = new HashMap<>();
		actionData.put(BranchActions.STEP_POSITION, 0L);
		actionData.put(BranchActions.EXECUTION_PLAN_ID, subFlowLevel2.getFlowUuid());
		ExecutionStep executionSplitStep = createExecutionStep(0L, "org.score.samples.controlactions.BranchActions", "split", actionData);
		executionSplitStep.setSplitStep(true);
		addNavigationToExecutionStep(1L, navigationActionClassName, simpleNavigationMethodName, executionSplitStep);
		executionPlan.addStep(executionSplitStep);

		ExecutionStep executionStep2 = createExecutionStep(1L, "org.score.samples.controlactions.BranchActions", "join", new HashMap<String, Serializable>());
		addNavigationToExecutionStep(3L, navigationActionClassName, simpleNavigationMethodName, executionStep2);
		executionPlan.addStep(executionStep2);

		//subflow2
		actionData = new HashMap<>();
		actionData.put(BranchActions.STEP_POSITION, 0L);
		actionData.put(BranchActions.EXECUTION_PLAN_ID, subFlowLevel3_1.getFlowUuid());
		executionSplitStep = createExecutionStep(3L, "org.score.samples.controlactions.BranchActions", "split", actionData);
		executionSplitStep.setSplitStep(true);
		addNavigationToExecutionStep(4L, navigationActionClassName, simpleNavigationMethodName, executionSplitStep);
		executionPlan.addStep(executionSplitStep);

		executionStep2 = createExecutionStep(4L, "org.score.samples.controlactions.BranchActions", "join", new HashMap<String, Serializable>());
		addNavigationToExecutionStep(2L, navigationActionClassName, simpleNavigationMethodName, executionStep2);
		executionPlan.addStep(executionStep2);

		actionData = new HashMap<>();
		actionData.put("message", "LEVEL 3");
		ExecutionStep executionStep3 = createExecutionStep(2L, "org.score.samples.controlactions.ConsoleControlActions", "echoHelloScore", actionData);

		executionPlan.addStep(executionStep3);

		return executionPlan;
	}

	private ExecutionPlan createSubflowLevel2(ExecutionPlan subFlowLevel1) {
		ExecutionPlan executionPlan = new ExecutionPlan();

		executionPlan.setFlowUuid("subFlowLevel2");

		executionPlan.setBeginStep(0L);

		Map<String, Serializable> actionData = new HashMap<>();
		actionData.put(BranchActions.STEP_POSITION, 0L);
		actionData.put(BranchActions.EXECUTION_PLAN_ID, subFlowLevel1.getFlowUuid());
		ExecutionStep executionSplitStep = createExecutionStep(0L, "org.score.samples.controlactions.BranchActions", "split", actionData);
		executionSplitStep.setSplitStep(true);
		addNavigationToExecutionStep(1L, navigationActionClassName, simpleNavigationMethodName, executionSplitStep);
		executionPlan.addStep(executionSplitStep);

		ExecutionStep executionStep2 = createExecutionStep(1L, "org.score.samples.controlactions.BranchActions", "join", new HashMap<String, Serializable>());
		addNavigationToExecutionStep(2L, navigationActionClassName, simpleNavigationMethodName, executionStep2);
		executionPlan.addStep(executionStep2);

		actionData = new HashMap<>();
		actionData.put("message", "LEVEL 2");
		ExecutionStep executionStep3 = createExecutionStep(2L, "org.score.samples.controlactions.ConsoleControlActions", "echoHelloScore", actionData);
		executionPlan.addStep(executionStep3);

		return executionPlan;
	}

	private static ExecutionStep createExecutionStep(Long stepId, String sessionActionClassName, String putObjectMethodName, Map<String, Serializable> actionData) {
		ExecutionStep executionPutDataStep = new ExecutionStep(stepId);
		executionPutDataStep.setAction(new ControlActionMetadata(sessionActionClassName, putObjectMethodName));
		executionPutDataStep.setActionData(actionData);
		return executionPutDataStep;
	}

	private static void addNavigationToExecutionStep(Long nextStepId, String navigationActionClassName, String navigationMethodName, ExecutionStep executionPutDataStep) {
		executionPutDataStep.setNavigation(new ControlActionMetadata(navigationActionClassName, navigationMethodName));
		Map<String, Serializable> navigationData = new HashMap<>();
		navigationData.put("nextStepId", nextStepId);
		executionPutDataStep.setNavigationData(navigationData);
	}

	private ExecutionPlan createSubflowLevel1() {
		return createSubFlow();
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
		//navigation test
		//subflow response should be found in the Execution Context -> goto success, else failure
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RESPONSE_KEY, SUCCESS_KEY, 2L)); //success
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 4L)); //failure
		Map<String, Serializable> subflowInputs = new HashMap<>();
		subflowInputs.put(MESSAGE_KEY, MESSAGE_SUBFLOW);
		TriggeringProperties triggeringProperties = TriggeringProperties.create(subFlow);
		triggeringProperties.setContext(subflowInputs);
		builder.addSubflow(0L, 1L, triggeringProperties, navigationMatchers);

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
		while(filterEventsQueue(eventList, SCORE_FINISHED_EVENT).size() != 2){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void waitForScoreToFinishMultipleLevels() {
		while(filterEventsQueue(eventList, "Hello score").size() != 2){
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