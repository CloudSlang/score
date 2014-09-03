package org.score.samples.openstack.actions;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.lang.ExecutionRuntimeServices;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.score.samples.openstack.actions.FinalStepActions.RESPONSE_KEY;
import static org.score.samples.openstack.actions.FinalStepActions.SUCCESS_KEY;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/executionPlanBuilderTestContext.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
	private List<ScoreEvent> eventList = Collections.synchronizedList(new ArrayList<ScoreEvent>());

	private static final long DEFAULT_TIMEOUT = 60000;

	@Autowired
	private Score score;

	@Autowired
	private EventBus eventBus;

	@Before
	public void init(){
		eventList = Collections.synchronizedList(new ArrayList<ScoreEvent>());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void testSubflow() throws Exception {
		ExecutionPlan subFlow = createSubFlow();
		TriggeringProperties parentFlowProperties = createParentFlow(subFlow);

		registerEventListener(ECHO_EVENT);

		score.trigger(parentFlowProperties);

		waitForAllEventsToArrive(2); //2 echo events should have been fired
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

	private void waitForAllEventsToArrive(int eventsCount) {
		while(eventList.size() < eventsCount){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void registerEventListener(String... eventTypes) {
		Set<String> handlerTypes = new HashSet<>();
		Collections.addAll(handlerTypes, eventTypes);
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logger.info("Listener " + this.toString() + " invoked on type: " + event.getEventType() + " with data: " + event.getData());
				eventList.add(event);
			}
		}, handlerTypes);
	}
}