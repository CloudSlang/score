package org.score.samples;

import com.hp.score.events.EventConstants;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
		    String command = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			while(!command.equals("4")) {
				System.out.println("Select command:");
				System.out.println("1 - Create server on OpenStack");
				System.out.println("2 - List servers from OpenStack");
				System.out.println("3 - Input missing scenario");
				System.out.println("4 - Quit\n");

				System.out.print("Command:\n");
				command = readLineDenyNulls(reader);

				switch (command) {

					case "1":
						createServerInputs(true);
						break;
					case "2":
						listServerInputs(true);
						break;
					case "3":
						listServerInputs(false);
						break;
					case "4":
						System.exit(0);
						break;
					default:
						System.out.println("Unknown command..");
						break;
				}
			}
	}

	private void listServerInputs(Boolean nullAllowed) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String username;
		String password;
		String host;
		String port;
		try {
			System.out.println("Host: ");
			host = readLineAdmitNulls(br);
			System.out.println("Port: ");
			port = br.readLine();
			System.out.println("Username: ");
			username = br.readLine();
			System.out.println("Password: ");
			password = br.readLine();
			listServers(host, port, username, password, nullAllowed);
		} catch (IOException ioe) {
			System.out.println("IO error");
			System.exit(1);
		}
	}

	private void createServerInputs(Boolean nullAllowed) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String username;
		String password;
		String host;
		String port;
		String serverName;
		try {
			System.out.println("Host: ");
			host = br.readLine();
			System.out.println("Port: ");
			port = br.readLine();
			System.out.println("Username: ");
			username = br.readLine();
			System.out.println("Password: ");
			password = br.readLine();
			System.out.println("Server name: ");
			serverName = br.readLine();
			createServer(host, port, serverName, username, password, nullAllowed);
		} catch (IOException ioe) {
			System.out.println("IO error");
			System.exit(1);
		}
	}

	private void createServer(String host, String port, String serverName, String username, String password, Boolean nullAllowed){
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
		Map<String, Serializable> executionContext = new HashMap<>();

		createGetTokenStep(host, port, username, password, builder, executionContext, nullAllowed);

		createContextMergerStep(builder, nullAllowed);

		startServerStep(serverName, builder, executionContext, nullAllowed);

		ExecutionPlan executionPlan = builder.getExecutionPlan();
		score.trigger(executionPlan, executionContext);
	}

	private void createGetTokenStep(String host, String port, String username, String password, ExecutionPlanBuilder builder, Map<String, Serializable> executionContext, Boolean nullAllowed){
		String url = "http://" + host + ":" + port + "/v2.0/tokens";
		String body = "{\"auth\": {\"tenantName\": \"demo\",\"passwordCredentials\": {\"username\": \"" + username +"\",\"password\": \"" + password + "\"}}}";

		executionContext.put("url", url);
		executionContext.put("method", "post");
		executionContext.put("body", body);
		executionContext.put("contentType", "application/json");

		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "returnCode", "0", 1L)); // how will we know the key
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 3L));

		builder.addOOActionStep(0L, "org.score.content.httpclient.HttpClientAction", "execute", nullAllowed, navigationMatchers);
	}

	private void createContextMergerStep(ExecutionPlanBuilder builder, Boolean nullAllowed) {
		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "result", "0", 2L)); // how will we know the key
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 3L));

		builder.addOOActionStep(1L, "org.score.samples.openstack.actions.ContextMerger", "prepareCreateServer", nullAllowed, navigationMatchers);
	}

	private void startServerStep(String serverName, ExecutionPlanBuilder builder, Map<String, Serializable> executionContext, Boolean nullAllowed){
		executionContext.put("serverName", serverName);
		executionContext.put("method", "post");

		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "returnCode", "0", 3L)); // how will we know the key
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 3L));

		builder.addOOActionStep(2L, "org.score.content.httpclient.HttpClientAction", "execute", nullAllowed, navigationMatchers);
		builder.addOOActionFinalStep(3L, "org.score.samples.openstack.actions.FinalStepActions", "successStepAction");
	}

	private void listServers(String host, String port, String username, String password, Boolean nullAllowed){
		ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
		Map<String, Serializable> executionContext = new HashMap<>();

		createGetTokenStep(host, port, username, password, builder, executionContext, nullAllowed);

		createPrepareGetServersStep(builder, nullAllowed);

		createGetServersStep(builder, nullAllowed);

		ExecutionPlan executionPlan = builder.getExecutionPlan();
		score.trigger(executionPlan, executionContext);
	}

	private void createPrepareGetServersStep(ExecutionPlanBuilder builder, Boolean nullAllowed) {
		//prepare context for get servers
		List<NavigationMatcher<Serializable>>  navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 2L));
		builder.addOOActionStep(1L, "org.score.samples.openstack.actions.ContextMerger", "prepareGetServer", nullAllowed, navigationMatchers);
	}

	private void createGetServersStep(ExecutionPlanBuilder builder, Boolean nullAllowed) {
		Long successStepId = 4L;
		Long displayStepId = 3L;
		List<NavigationMatcher<Serializable>>  navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "statusCode", "200", displayStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "statusCode", "203", displayStepId));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, displayStepId));
		builder.addOOActionStep(2L, "org.score.content.httpclient.HttpClientAction", "execute", nullAllowed, navigationMatchers);

		//display step
		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, successStepId));
		builder.addOOActionStep(displayStepId, "org.score.samples.openstack.actions.ContextMerger", "getServerNames", nullAllowed, navigationMatchers);

		//success step
		builder.addOOActionFinalStep(successStepId, "org.score.samples.openstack.actions.FinalStepActions", "successStepAction");
	}

	private String readLineDenyNulls(BufferedReader reader) {
		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException ioe) {
			System.out.println("IO error trying to read command");
			System.exit(1);
		}
		return line;
	}

	private String readLineAdmitNulls(BufferedReader reader) {
		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException ioe) {
			System.out.println("IO error trying to read command");
			System.exit(1);
		}
		return line.isEmpty() ? null : line;
	}

	private static OpenstackApplications loadApp() {
		ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/openstackApplicationContext.xml");
		OpenstackApplications app = context.getBean(OpenstackApplications.class);
		app.context  = context;
		return app;
	}

	@SuppressWarnings("unused")
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
				logListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void registerInfoEventListener(Set<String> handlerTypes) {
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void registerScoreEventListener() {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
		handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
		handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
		eventBus.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				logScoreListenerEvent(event);
			}
		}, handlerTypes);
	}

	private void logListenerEvent(ScoreEvent event) {
		logger.info("Event " + event.getEventType() + " occurred: " + event.getData());
	}

	private void logScoreListenerEvent(ScoreEvent event) {
		logger.info("Event " + event.getEventType() + " occurred");
	}
}
