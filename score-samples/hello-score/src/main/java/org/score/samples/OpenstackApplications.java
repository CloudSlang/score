package org.score.samples;

import org.score.samples.openstack.actions.ExecutionPlanBuilder;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.Score;
import com.hp.score.events.EventBus;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import org.apache.log4j.Logger;
import org.score.samples.openstack.actions.MatchType;
import org.score.samples.openstack.actions.NavigationMatcher;
import org.score.samples.openstack.actions.OOActionRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 7/28/2014
 *
 * @author Bonczidai Levente
 */
public class OpenstackApplications {
	private final static Logger logger = Logger.getLogger(OpenstackApplications.class);
	private ApplicationContext context;
	private Long executionID;

	@Autowired
	private Score score;

	@Autowired
	private EventBus eventBus;

	public static void main(String[] args) {
		OpenstackApplications app = loadApp();
		app.registerEventListeners();
		app.start();
	}

	private void start() {
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
		List<NavigationMatcher> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher(MatchType.COMPARE_EQUAL, "result", "200", "1"));
		//navigationMatchers.add(new NavigationMatcher(MatchType.COMPARE_NOT_EQUAL, "result", "200", "2"));y


		builder.addStep(0L, "org.score.samples.openstack.actions.HttpClientPostMock", "post", navigationMatchers, "2");

		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher(MatchType.COMPARE_EQUAL, "result", "400", "2"));

		builder.addStep(1L, "org.score.samples.openstack.actions.HttpClientSendEmailMock", "sendEmail", navigationMatchers, "2");

		navigationMatchers = null;

		builder.addStep(2L, "org.score.samples.openstack.actions.ReturnStepActions", "successStepAction", navigationMatchers, "2");

		ExecutionPlan executionPlan = builder.getExecutionPlan();

		Map<String, Serializable> executionContext = new HashMap<>();
		prepareExecutionContext(executionContext);

		executionID = score.trigger(executionPlan, executionContext);
	}

	private void prepareExecutionContext(Map<String, Serializable> executionContext) {
		//for post
		executionContext.put("username", "userTest");
		executionContext.put("password", "passTest");
		executionContext.put("host", "hostTest");
		executionContext.put("url", "urlTest");
		//for sendEmail
		executionContext.put("receiver", "receiverTest");
		executionContext.put("title", "titleTest");
		executionContext.put("body", "bodyTest");
	}

	private static OpenstackApplications loadApp() {
		ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/openstackApplicationContext.xml");
		OpenstackApplications app = context.getBean(OpenstackApplications.class);
		app.context  = context;
		return app;
	}

	private void closeContext() {
		((ConfigurableApplicationContext) context).close();
	}

	private void registerEventListeners() {
		//register listener for action runtime events
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(OOActionRunner.ACTION_RUNTIME_EVENT_TYPE);
		registerInfoEventListener(handlerTypes);

		//register listener for action exception events
		registerExceptionEventListener();

		// for closing the Application Context when score finishes execution
		registerScoreEventListener();
	}

	private void registerExceptionEventListener() {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(OOActionRunner.ACTION_EXCEPTION_EVENT_TYPE);
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logger.info("Listener " + this.toString() + " invoked on type: " + event.getEventType() + " with data: " + event.getData());
				logger.info("Attempting to cancel the execution");
				score.cancelExecution(executionID);
			}
		}, handlerTypes);
	}

	private void registerInfoEventListener(Set<String> handlerTypes) {
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logger.info("Listener " + this.toString() + " invoked on type: " + event.getEventType() + " with data: " + event.getData());
			}
		}, handlerTypes);
	}

	private void registerScoreEventListener() {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes = new HashSet<>();
		handlerTypes.add("FINISHED");
		handlerTypes.add("ERROR");
		handlerTypes.add("CANCELLED");
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logger.info("Listener " + this.toString() + " invoked on type: " + event.getEventType() + " with data: " + event.getData());
				closeContext();
			}
		}, handlerTypes);
	}
}
