/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cloudslang.samples;

import io.cloudslang.score.api.ControlActionMetadata;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import io.cloudslang.score.api.Score;
import io.cloudslang.score.api.TriggeringProperties;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private Score score;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private EventBus eventBus;

    private final static Logger logger = LogManager.getLogger(HelloScore.class);
    private ApplicationContext context;
    private final Object lock = new Object();

    public static void main(String[] args) {
        HelloScore app = loadApp();
        app.registerEventListener();
        app.start();
        System.exit(0);
    }

    private static HelloScore loadApp() {
        ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/helloScoreContext.xml");
        HelloScore app = context.getBean(HelloScore.class);
        app.context  = context;
        return app;
    }

    private void start() {
        ExecutionPlan executionPlan = createExecutionPlan();
        score.trigger(TriggeringProperties.create(executionPlan));
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
        executionPlan.setName("Sample name");
        executionPlan.setLanguage("Sample lang");
        executionPlan.setBeginStep(0L);

        ExecutionStep executionStep = new ExecutionStep(0L);
        executionStep.setAction(new ControlActionMetadata("io.cloudslang.samples.controlactions.ConsoleControlActions", "echoHelloScore"));
        executionStep.setActionData(new HashMap<String, Serializable>());
        executionStep.setNavigation(new ControlActionMetadata("io.cloudslang.samples.controlactions.NavigationActions", "nextStepNavigation"));
        executionStep.setNavigationData(new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep);

        ExecutionStep executionStep2 = new ExecutionStep(1L);
        executionStep2.setAction(new ControlActionMetadata("io.cloudslang.samples.controlactions.ConsoleControlActions", "echoHelloScore"));
        executionStep2.setActionData(new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep2);

        return executionPlan;
    }

    private void registerEventListener() {
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        eventBus.subscribe(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Listener " + this.toString() + " invoked on type: " + event.getEventType() + " with data: " + event.getData());
                } else {
                    logger.info("Listener " + this.toString() + " invoked on type: " + event.getEventType());
                }
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