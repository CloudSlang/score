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

import com.google.common.collect.Sets;
import io.cloudslang.samples.controlactions.BranchActions;
import io.cloudslang.samples.controlactions.SessionDataActions;
import io.cloudslang.score.api.ControlActionMetadata;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import io.cloudslang.score.api.Score;
import io.cloudslang.score.api.TriggeringProperties;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

/**
 * User: stoneo
 * Date: 22/07/2014
 * Time: 14:42
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/samples/schemaAllTestContext.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class StandAloneTest {

    @Autowired
    private Score score;

    @Autowired
    private EventBus eventBus;


    private List<ScoreEvent> eventQueue = Collections.synchronizedList(new ArrayList<ScoreEvent>());

    private final static Logger logger = Logger.getLogger(StandAloneTest.class);

    private final static String simpleNavigationMethodName = "simpleNavigation";
    private final static String navigationActionClassName = "io.cloudslang.samples.controlactions.NavigationActions";


    @Before
    public void init(){
        eventQueue = Collections.synchronizedList(new ArrayList<ScoreEvent>());
    }

    @PostConstruct
    public void waitToWorkerToBeUp() throws InterruptedException {
        Thread.sleep(1000L);
    }

    @Test(timeout = 20000)
    public void baseStandAloneTest() {
        ExecutionPlan executionPlan = createExecutionPlan();
        TriggeringProperties triggeringProperties = TriggeringProperties.create(executionPlan);
        registerEventListener(EventConstants.SCORE_FINISHED_EVENT);
        long executionId = score.trigger(triggeringProperties);

        waitForAllEventsToArrive(1);
        long finishEventExecutionId = (Long)((Map)eventQueue.get(0).getData()).get(EventConstants.EXECUTION_ID_CONTEXT);
        Assert.assertNotNull(finishEventExecutionId);
        assertEquals(executionId, finishEventExecutionId);
    }

    @Test(timeout = 20000)
    public void subFlowTest() {
        ExecutionPlan executionPlan = createFlowWithSubflowExecutionPlan();
        ExecutionPlan subFlowExecutionPlan = createExecutionPlan();
        executionPlan.setSubflowsUUIDs(Sets.newHashSet(subFlowExecutionPlan.getFlowUuid()));
        TriggeringProperties triggeringProperties = TriggeringProperties.create(executionPlan);
        Map<String, ExecutionPlan> dependencies = new HashMap<>();
        dependencies.put(subFlowExecutionPlan.getFlowUuid(),subFlowExecutionPlan);
        triggeringProperties.setDependencies(dependencies);
        Map<String,Serializable> getRuntimeValues = new HashMap<>();
        getRuntimeValues.put("STEP_TYPE", "PARALLEL");
        triggeringProperties.setRuntimeValues(getRuntimeValues);
        registerEventListener("Hello score");

        score.trigger(triggeringProperties);

        waitForAllEventsToArrive(2);//this flow should have 2 "Hello score" events only
    }

    @Test(timeout = 20000)
    public void testParallelFlow(){
        ExecutionPlan executionPlan = createParallelFlow();
        ExecutionPlan branchExecutionPlan = createExecutionPlan();
        executionPlan.setSubflowsUUIDs(Sets.newHashSet(branchExecutionPlan.getFlowUuid()));
        TriggeringProperties triggeringProperties = TriggeringProperties.create(executionPlan);
        triggeringProperties.getDependencies().put(branchExecutionPlan.getFlowUuid(),branchExecutionPlan);

        Map<String,Serializable> getRuntimeValues = new HashMap<>();
        getRuntimeValues.put("STEP_TYPE", "PARALLEL");
        triggeringProperties.setRuntimeValues(getRuntimeValues);
        registerEventListener("Hello score",EventConstants.SCORE_FINISHED_EVENT,EventConstants.SCORE_FINISHED_BRANCH_EVENT);

        score.trigger(triggeringProperties);

        waitForAllEventsToArrive(5);

        assertEquals(2,countEvents("Hello score"));
        assertEquals(1,countEvents(EventConstants.SCORE_FINISHED_EVENT));
        assertEquals(2,countEvents(EventConstants.SCORE_FINISHED_BRANCH_EVENT));
    }


    @Test(timeout = 20000)
    public void useExecutionSessionDataTest() {
        ExecutionPlan executionPlan = createSessionDataExecutionPlan();
        TriggeringProperties triggeringProperties = TriggeringProperties.create(executionPlan);
        registerEventListener(EventConstants.SCORE_FINISHED_EVENT, SessionDataActions.SESSION_BEFORE_PUT_DATA_EVENT, SessionDataActions.SESSION_GET_DATA_EVENT);
        score.trigger(triggeringProperties);

        waitForAllEventsToArrive(3);
        ScoreEvent sessionBeforePutEvent = getEventFromQueueByType(SessionDataActions.SESSION_BEFORE_PUT_DATA_EVENT);
        assertEquals(sessionBeforePutEvent.getData(), null);
        ScoreEvent sessionGetEvent = getEventFromQueueByType(SessionDataActions.SESSION_GET_DATA_EVENT);
        assertEquals(sessionGetEvent.getData(), SessionDataActions.TEST_VALUE);
    }

    @Test(timeout = 50000)
    public void shareSessionDataWithSubflowTest() {
        ExecutionPlan executionPlan = createParentPutOnSessionExecutionPlan("childGetFromSessionFlow");
        ExecutionPlan subFlowExecutionPlan = createChildGetFromSessionExecutionPlan();
        executionPlan.setSubflowsUUIDs(Sets.newHashSet(subFlowExecutionPlan.getFlowUuid()));
        Map<String, ExecutionPlan> dependencies = new HashMap<>();
        dependencies.put(subFlowExecutionPlan.getFlowUuid(), subFlowExecutionPlan);

        Map<String,Serializable> getRuntimeValues = new HashMap<>();
        getRuntimeValues.put("STEP_TYPE", "PARALLEL");

        TriggeringProperties triggeringProperties = TriggeringProperties.create(executionPlan).
                setDependencies(dependencies).setRuntimeValues(getRuntimeValues);

        registerEventListener(EventConstants.SCORE_FINISHED_EVENT, SessionDataActions.SESSION_BEFORE_PUT_DATA_EVENT, SessionDataActions.SESSION_GET_DATA_EVENT);
        score.trigger(triggeringProperties);

        waitForAllEventsToArrive(3);
        ScoreEvent sessionBeforePutEvent = getEventFromQueueByType(SessionDataActions.SESSION_BEFORE_PUT_DATA_EVENT);
        assertEquals(null, sessionBeforePutEvent.getData());
        ScoreEvent sessionGetEvent = getEventFromQueueByType(SessionDataActions.SESSION_GET_DATA_EVENT);
        assertEquals(SessionDataActions.TEST_VALUE, sessionGetEvent.getData());
    }

    @Test(timeout = 20000)
    public void sessionDataNotSharedBetweenExecutions() {
        ExecutionPlan putDataExecutionPlan = new ExecutionPlan();
        putDataExecutionPlan.setFlowUuid("putDataOnSessionFlow");
        putDataExecutionPlan.setBeginStep(0L);

        ExecutionStep executionPutDataStep = createPutDataOnSessionStep(0L, null);
        putDataExecutionPlan.addStep(executionPutDataStep);

        ExecutionPlan getDataExecutionPlan = new ExecutionPlan();
        getDataExecutionPlan.setFlowUuid("getDataFromSessionFlow");
        getDataExecutionPlan.setBeginStep(0L);
        ExecutionStep executionGetDataStep = createGetDataFromSessionStep(0L);
        getDataExecutionPlan.addStep(executionGetDataStep);

        TriggeringProperties putTriggeringProperties = TriggeringProperties.create(putDataExecutionPlan);
        TriggeringProperties getTriggeringProperties = TriggeringProperties.create(getDataExecutionPlan);
        registerEventListener(EventConstants.SCORE_FINISHED_EVENT, SessionDataActions.SESSION_BEFORE_PUT_DATA_EVENT, SessionDataActions.SESSION_GET_DATA_EVENT);
        score.trigger(putTriggeringProperties);
        score.trigger(getTriggeringProperties);
        waitForAllEventsToArrive(4);

        ScoreEvent sessionBeforePutEvent = getEventFromQueueByType(SessionDataActions.SESSION_BEFORE_PUT_DATA_EVENT);
        assertEquals(sessionBeforePutEvent.getData(), null);
        ScoreEvent sessionGetEvent = getEventFromQueueByType(SessionDataActions.SESSION_GET_DATA_EVENT);
        assertEquals(sessionGetEvent.getData(), null);
    }

    private ScoreEvent getEventFromQueueByType(String eventType){

        for(ScoreEvent event : eventQueue){
            if(event.getEventType().equals(eventType))
                return event;
        }
        return null;
    }

     private void waitForAllEventsToArrive(int eventsCount) {
        while(eventQueue.size() != eventsCount){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int countEvents(String eventType) {
        int i = 0;
        for(ScoreEvent event:eventQueue){
            if(event.getEventType().equalsIgnoreCase(eventType)){
                i++;
            }
        }
        return i;
    }

    private static ExecutionPlan createExecutionPlan() {
        ExecutionPlan executionPlan = new ExecutionPlan();

        executionPlan.setFlowUuid("1");

        executionPlan.setBeginStep(0L);

        ExecutionStep executionStep = createExecutionStep(0L, "io.cloudslang.samples.controlactions.ConsoleControlActions", "echoHelloScore", new HashMap<String, Serializable>());
        addNavigationToExecutionStep(1L, navigationActionClassName, simpleNavigationMethodName, executionStep);
        executionPlan.addStep(executionStep);

        ExecutionStep executionStep2 = createExecutionStep(1L, "io.cloudslang.samples.controlactions.ConsoleControlActions", "echoHelloScore", new HashMap<String, Serializable>());
        executionPlan.addStep(executionStep2);

        return executionPlan;
    }

    private static ExecutionPlan createFlowWithSubflowExecutionPlan() {
        ExecutionPlan executionPlan = new ExecutionPlan();

        executionPlan.setFlowUuid("parentFlow");

        executionPlan.setBeginStep(0L);

        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(BranchActions.STEP_POSITION, 1L);
        actionData.put(BranchActions.EXECUTION_PLAN_ID, "1");
        ExecutionStep executionSplitStep = createExecutionStep(0L, "io.cloudslang.samples.controlactions.BranchActions", "split", actionData);
        executionSplitStep.setSplitStep(true);
        addNavigationToExecutionStep(1L, navigationActionClassName, simpleNavigationMethodName, executionSplitStep);
        executionPlan.addStep(executionSplitStep);

        ExecutionStep executionStep2 = createExecutionStep(1L, "io.cloudslang.samples.controlactions.BranchActions", "join", new HashMap<String, Serializable>());
        addNavigationToExecutionStep(2L, navigationActionClassName, simpleNavigationMethodName, executionStep2);
        executionPlan.addStep(executionStep2);

        ExecutionStep executionStep3 = createExecutionStep(2L, "io.cloudslang.samples.controlactions.ConsoleControlActions", "echoHelloScore", actionData);
        executionPlan.addStep(executionStep3);

        return executionPlan;
    }

    private ExecutionPlan createParallelFlow() {
        ExecutionPlan executionPlan = new ExecutionPlan();

        executionPlan.setFlowUuid("parallelFlow");

        executionPlan.setBeginStep(0L);

        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(BranchActions.STEP_POSITION, 1L);
        actionData.put(BranchActions.EXECUTION_PLAN_ID, "1");
        ExecutionStep executionSplitStep = createExecutionStep(0L, "io.cloudslang.samples.controlactions.BranchActions", "parallelSplit", actionData);
        executionSplitStep.setSplitStep(true);
        addNavigationToExecutionStep(1L, navigationActionClassName, simpleNavigationMethodName, executionSplitStep);
        executionPlan.addStep(executionSplitStep);

        ExecutionStep executionStep2 = createExecutionStep(1L, "io.cloudslang.samples.controlactions.BranchActions", "join", new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep2);

        return executionPlan;
    }

    private static ExecutionPlan createSessionDataExecutionPlan() {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setFlowUuid("basicSessionDataFlow");
        executionPlan.setBeginStep(0L);

        ExecutionStep executionPutDataStep = createPutDataOnSessionStep(0L, 1L);
        executionPlan.addStep(executionPutDataStep);

//        ExecutionStep sleepDataStep = createExecutionStep(1L, "io.cloudslang.samples.controlactions.SessionDataActions", "sleepAction", new HashMap<String, Serializable>());
//        addNavigationToExecutionStep(2L, navigationActionClassName, simpleNavigationMethodName, sleepDataStep);
//        executionPlan.addStep(sleepDataStep);

        ExecutionStep executionGetDataStep = createGetDataFromSessionStep(1L);
        executionPlan.addStep(executionGetDataStep);
        return executionPlan;
    }

    private static ExecutionStep createGetDataFromSessionStep(Long stepId) {
        return createExecutionStep(stepId, "io.cloudslang.samples.controlactions.SessionDataActions", "getObject", new HashMap<String, Serializable>());
    }

    private static ExecutionStep createPutDataOnSessionStep(Long stepId, Long nextStepId) {
        ExecutionStep executionPutDataStep = createExecutionStep(stepId, "io.cloudslang.samples.controlactions.SessionDataActions", "putObject", new HashMap<String, Serializable>());
        if(nextStepId != null)
            addNavigationToExecutionStep(nextStepId, navigationActionClassName, simpleNavigationMethodName, executionPutDataStep);
        return executionPutDataStep;
    }

    private ExecutionPlan createParentPutOnSessionExecutionPlan(String childFlowId) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setFlowUuid("parentPutOnSessionFlow");
        executionPlan.setBeginStep(0L);

        ExecutionStep executionPutDataStep = createPutDataOnSessionStep(0L, 1L);
        executionPlan.addStep(executionPutDataStep);

        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(BranchActions.STEP_POSITION, 0L);
        actionData.put(BranchActions.EXECUTION_PLAN_ID, childFlowId);

        ExecutionStep executionSplitStep = createExecutionStep(1L, "io.cloudslang.samples.controlactions.BranchActions", "split", actionData);
        executionSplitStep.setSplitStep(true);
        addNavigationToExecutionStep(2L, navigationActionClassName, simpleNavigationMethodName, executionSplitStep);
        executionPlan.addStep(executionSplitStep);

        ExecutionStep executionJoinStep = createExecutionStep(2L, "io.cloudslang.samples.controlactions.BranchActions", "join", new HashMap<String, Serializable>());
        executionPlan.addStep(executionJoinStep);

        return executionPlan;
    }

    private ExecutionPlan createChildGetFromSessionExecutionPlan(){

        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setFlowUuid("childGetFromSessionFlow");
        executionPlan.setBeginStep(0L);

        ExecutionStep executionGetDataStep = createGetDataFromSessionStep(0L);
        executionPlan.addStep(executionGetDataStep);

        return executionPlan;
    }

    private static void addNavigationToExecutionStep(Long nextStepId, String navigationActionClassName, String navigationMethodName, ExecutionStep executionPutDataStep) {
        executionPutDataStep.setNavigation(new ControlActionMetadata(navigationActionClassName, navigationMethodName));
        Map<String, Serializable> navigationData = new HashMap<>();
        navigationData.put("nextStepId", nextStepId);
        executionPutDataStep.setNavigationData(navigationData);
    }

    private static ExecutionStep createExecutionStep(Long stepId, String sessionActionClassName, String putObjectMethodName, Map<String, Serializable> actionData) {
        ExecutionStep executionPutDataStep = new ExecutionStep(stepId);
        executionPutDataStep.setAction(new ControlActionMetadata(sessionActionClassName, putObjectMethodName));
        executionPutDataStep.setActionData(actionData);
        return executionPutDataStep;
    }

    private void registerEventListener(String... eventTypes) {
        Set<String> handlerTypes = new HashSet<>();
        Collections.addAll(handlerTypes, eventTypes);
        eventBus.subscribe(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                logger.info("Listener " + this.toString() + " invoked on type: " + event.getEventType() + " with data: " + event.getData());
                eventQueue.add(event);
            }
        }, handlerTypes);
    }
}
