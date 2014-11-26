/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.samples;

import org.eclipse.score.api.ControlActionMetadata;
import org.eclipse.score.api.ExecutionPlan;
import org.eclipse.score.api.ExecutionStep;
import org.eclipse.score.api.Score;
import org.eclipse.score.api.TriggeringProperties;
import org.eclipse.score.events.EventBus;
import org.eclipse.score.events.EventConstants;
import org.eclipse.score.events.ScoreEvent;
import org.eclipse.score.events.ScoreEventListener;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * User:
 * Date: 22/06/2014
 */
public class HelloScore {

    @Autowired
    private Score score;

    @Autowired
    private EventBus eventBus;

    private final static Logger logger = Logger.getLogger(HelloScore.class);
    private ApplicationContext context;
    private Long executionID;
    private final Object lock = new Object();

    public static void main(String[] args) {
        HelloScore app = loadApp();
        app.registerEventListener();
        app.start();

    }

    private static HelloScore loadApp() {
        ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/helloScoreContext.xml");
        HelloScore app = context.getBean(HelloScore.class);
        app.context  = context;
        return app;
    }

    private void start() {
        ExecutionPlan executionPlan = createExecutionPlan();
        executionID = score.trigger(TriggeringProperties.create(executionPlan));
        waitForExecutionToFinish();
        closeContext();
    }

    private void waitForExecutionToFinish() {
        try {
            synchronized(lock){
                lock.wait(10000);
            }
        } catch (InterruptedException e) {
            logger.error(e.getStackTrace());
        }
    }

    private static ExecutionPlan createExecutionPlan() {
        ExecutionPlan executionPlan = new ExecutionPlan();

        executionPlan.setFlowUuid("1");

        executionPlan.setBeginStep(0L);

        ExecutionStep executionStep = new ExecutionStep(0L);
        executionStep.setAction(new ControlActionMetadata("org.eclipse.score.samples.controlactions.ConsoleControlActions", "echoHelloScore"));
        executionStep.setActionData(new HashMap<String, Serializable>());
        executionStep.setNavigation(new ControlActionMetadata("org.eclipse.score.samples.controlactions.NavigationActions", "nextStepNavigation"));
        executionStep.setNavigationData(new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep);

        ExecutionStep executionStep2 = new ExecutionStep(1L);
        executionStep2.setAction(new ControlActionMetadata("org.eclipse.score.samples.controlactions.ConsoleControlActions", "echoHelloScore"));
        executionStep2.setActionData(new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep2);

        return executionPlan;
    }

    private void registerEventListener() {
        Set<String> handlerTypes = new HashSet();
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        eventBus.subscribe(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                logger.info("Listener " + this.toString() + " invoked on type: " + event.getEventType() + " with data: " + event.getData());
                synchronized (lock) {
                    lock.notify();
                }
            }
        }, handlerTypes);
    }

    private void closeContext() {
        ((ConfigurableApplicationContext) context).close();
    }

}
