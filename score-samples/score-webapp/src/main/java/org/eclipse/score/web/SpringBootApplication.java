/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.web;

import org.eclipse.score.events.EventConstants;
import org.eclipse.score.events.ScoreEvent;
import org.eclipse.score.events.ScoreEventListener;
import org.eclipse.score.samples.openstack.actions.OOActionRunner;
import org.eclipse.score.web.controller.ScoreController;
import org.eclipse.score.web.services.ScoreServices;

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
@ComponentScan({"org.eclipse.score.web.controller","org.eclipse.score.web.services"})
public class SpringBootApplication {

    public static final String WEB_APP_CONTEXTS_XML = "META-INF.spring/webappContexts.xml";

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
            scoreContext = new ClassPathXmlApplicationContext(WEB_APP_CONTEXTS_XML);
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