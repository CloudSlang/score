package com.hp.score.web;

import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.samples.openstack.actions.OOActionRunner;
import com.hp.score.web.controller.ScoreController;
import com.hp.score.web.services.ScoreServices;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Date: 9/9/2014
 *
 * @author Bonczidai Levente
 */
@Configuration
@EnableAutoConfiguration(exclude={LiquibaseAutoConfiguration.class})
@ComponentScan({"com.hp.score.web.controller","com.hp.score.web.services"})
public class SpringBootApplication {
    public static final String SPRING_WEB_APPLICATION_CONTEXT_XML_PATH = "META-INF.spring/webApplicationContext.xml";
    private static final Logger logger = Logger.getLogger(SpringBootApplication.class);
    private ScoreServices scoreServices;

    public static void main(String[] args) {
        ApplicationContext springBootContext;
        ApplicationContext scoreContext;
        try {
            // load spring boot context
            springBootContext = SpringApplication.run(SpringBootApplication.class, args);
            SpringBootApplication springBootApplication = springBootContext.getBean(SpringBootApplication.class);
            ScoreController scoreController = springBootContext.getBean(ScoreController.class);

            //load score context
            scoreContext = new ClassPathXmlApplicationContext(SPRING_WEB_APPLICATION_CONTEXT_XML_PATH);
            springBootApplication.scoreServices = scoreContext.getBean(ScoreServices.class);
            scoreController.setScoreServices(springBootApplication.scoreServices);
            springBootApplication.registerEventListeners(springBootApplication.scoreServices);
        } catch (Exception | ClassFormatError ex) {
            logger.error(ex);
        }
    }

    private void registerEventListeners(ScoreServices scoreServices) {
        registerOOActionRunnerEventListener(scoreServices);
        registerExceptionEventListener(scoreServices);
        registerScoreEventListener(scoreServices);
    }

    private void registerOOActionRunnerEventListener(ScoreServices scoreServices) {
        Set<String> handlerTypes = new HashSet<>(1);
        handlerTypes.add(OOActionRunner.ACTION_RUNTIME_EVENT_TYPE);
        scoreServices.subscribe(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                handleEvent(event, true);
            }
        }, handlerTypes);
    }

    private void registerExceptionEventListener(ScoreServices scoreServices) {
        Set<String> handlerTypes = new HashSet<>(1);
        handlerTypes.add(OOActionRunner.ACTION_EXCEPTION_EVENT_TYPE);
        scoreServices.subscribe(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                handleEvent(event, true);
            }
        }, handlerTypes);
    }

    private void registerScoreEventListener(final ScoreServices scoreServices) {
        Set<String> handlerTypes = new HashSet<>(3);
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        scoreServices.subscribe(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                handleEvent(event, false);
            }
        }, handlerTypes);
    }

    private void handleEvent(ScoreEvent event, boolean displayData) {
        String eventString = getEventAsString(event, displayData);
        logger.info(eventString);
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