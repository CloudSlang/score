package com.hp.score.web;

import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.samples.openstack.actions.OOActionRunner;
import com.hp.score.web.controller.ScoreController;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Date: 9/9/2014
 *
 * @author Bonczidai Levente
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class SpringBootApplication {
	public static final String SPRING_WEB_APPLICATION_CONTEXT_XML_PATH = "META-INF.spring/webApplicationContext.xml";
	private ScoreService scoreService;

	public static void main(String[] args) {
		ApplicationContext springBootContext;
		ApplicationContext scoreContext;
		try {
			// load spring boot context
			springBootContext = SpringApplication.run(SpringBootApplication.class, args);
			SpringBootApplication springBootApplication = getBeanFromContext(springBootContext, SpringBootApplication.class);
			ScoreController scoreController = getBeanFromContext(springBootContext, ScoreController.class);
			printBeans(springBootContext, "SpringBoot context");

			//load score context
			scoreContext = loadScoreContext();
			springBootApplication.scoreService = getBeanFromContext(scoreContext, ScoreService.class);
			springBootApplication.scoreService.setScoreController(scoreController);
			scoreController.setScoreService(springBootApplication.scoreService);
			springBootApplication.registerEventListeners(springBootApplication.scoreService);
			printBeans(scoreContext, "Score context");
		} catch (Exception | ClassFormatError ex) {
			ex.printStackTrace();
		}
	}

	private static <T> T getBeanFromContext(ApplicationContext context, Class<T> beanClass) throws NoSuchBeanDefinitionException{
		return context.getBean(beanClass);
	}

	private static ApplicationContext loadScoreContext() {
		return new ClassPathXmlApplicationContext(SPRING_WEB_APPLICATION_CONTEXT_XML_PATH);
	}

	private static void printBeans(ApplicationContext ctx, String contextName) {
		System.out.println("Beans from " + contextName + ":");
		String[] beanNames = ctx.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			System.out.println(beanName);
		}
	}

	private void registerEventListeners(ScoreService scoreService) {
		registerOOActionRunnerEventListener(scoreService);
		registerExceptionEventListener(scoreService);
		registerScoreEventListener(scoreService);
	}

	private void registerOOActionRunnerEventListener(ScoreService scoreService) {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(OOActionRunner.ACTION_RUNTIME_EVENT_TYPE);
		scoreService.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				handleEvent(event, true);
			}
		}, handlerTypes);
	}

	private void registerExceptionEventListener(ScoreService scoreService) {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(OOActionRunner.ACTION_EXCEPTION_EVENT_TYPE);
		scoreService.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				handleEvent(event, true);
			}
		}, handlerTypes);
	}

	private void registerScoreEventListener(final ScoreService scoreService) {
		Set<String> handlerTypes = new HashSet<>();
		handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
		handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
		handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
		scoreService.subscribe(new ScoreEventListener() {
			@Override
			public void onEvent(ScoreEvent event) {
				handleEvent(event, false);
				scoreService.setFlowRunning(false);
			}
		}, handlerTypes);
	}

	private void handleEvent(ScoreEvent event, boolean displayData) {
		String eventString = getEventAsString(event, displayData);
		printToConsole(eventString);
		addToHtmlOutput(eventString);
	}

	private void printToConsole(String message) {
		System.out.println(message);
	}

	private void addToHtmlOutput(String message) {
		scoreService.addTextOutput(message);
	}

	private String getEventAsString(ScoreEvent event, boolean displayData) {
		String message;
		if (displayData) {
			message = "Event " + event.getEventType() + " occurred: " + event.getData();
		} else {
			message = "Event " + event.getEventType() + " occurred";
		}
		return message;
	}
}