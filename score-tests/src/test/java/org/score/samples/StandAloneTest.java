package org.score.samples;

import com.google.common.collect.Sets;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.score.samples.controlactions.BranchActions;
import org.score.samples.controlactions.SessionDataActions;
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
    private final static String navigationActionClassName = "org.score.samples.controlactions.NavigationActions";

    @Before
    public void init(){
        eventQueue = new ArrayList<>();
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
        long finishEventExecutionId = (Long)((Map)eventQueue.get(0).getData()).get(ExecutionConstants.EXECUTION_ID_CONTEXT);
        Assert.assertNotNull(finishEventExecutionId);
        Assert.assertEquals(executionId, finishEventExecutionId);
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
        getRuntimeValues.put("NEW_BRANCH_MECHANISM",Boolean.TRUE);//TODO - remove this !! needs to work with this on by default, pending Non-Blocking story
        triggeringProperties.setRuntimeValues(getRuntimeValues);
        registerEventListener("Hello score");

        score.trigger(triggeringProperties);

        waitForAllEventsToArrive(2);//this flow should have 2 "Hello score" events only
    }

    @Test
    public void testParallelFlow(){
        ExecutionPlan executionPlan = createParallelFlow();
        ExecutionPlan branchExecutionPlan = createExecutionPlan();
        executionPlan.setSubflowsUUIDs(Sets.newHashSet(branchExecutionPlan.getFlowUuid()));
        TriggeringProperties triggeringProperties = TriggeringProperties.create(executionPlan);
        triggeringProperties.getDependencies().put(branchExecutionPlan.getFlowUuid(),branchExecutionPlan);
        Map<String,Serializable> getRuntimeValues = new HashMap<>();
        getRuntimeValues.put("NEW_BRANCH_MECHANISM",Boolean.TRUE);//TODO - remove this !! needs to work with this on by default, pending Non-Blocking story
        triggeringProperties.setRuntimeValues(getRuntimeValues);
        registerEventListener("Hello score");

        score.trigger(triggeringProperties);

        waitForAllEventsToArrive(2);//this flow should have 2 "Hello score" events only
    }


    @Test(timeout = 20000)
    public void useExecutionSessionDataTest() {
        ExecutionPlan executionPlan = createSessionDataExecutionPlan();
        TriggeringProperties triggeringProperties = TriggeringProperties.create(executionPlan);
        registerEventListener(EventConstants.SCORE_FINISHED_EVENT, SessionDataActions.SESSION_BEFORE_PUT_DATA_EVENT, SessionDataActions.SESSION_GET_DATA_EVENT);
        score.trigger(triggeringProperties);

        waitForAllEventsToArrive(3);
        ScoreEvent sessionBeforePutEvent = getEventFromQueueByType(SessionDataActions.SESSION_BEFORE_PUT_DATA_EVENT);
        Assert.assertEquals(sessionBeforePutEvent.getData(), null);
        ScoreEvent sessionGetEvent = getEventFromQueueByType(SessionDataActions.SESSION_GET_DATA_EVENT);
        Assert.assertEquals(sessionGetEvent.getData(), SessionDataActions.TEST_VALUE);
    }

//    @Test//(timeout = 20000)
//    public void shareSessionDataWithSubflowTest() {
//        ExecutionPlan executionPlan = createParentPutOnSessionExecutionPlan("childGetFromSessionFlow");
//        ExecutionPlan subFlowExecutionPlan = createChildGetFromSessionExecutionPlan();
//        executionPlan.setSubflowsUUIDs(Sets.newHashSet(subFlowExecutionPlan.getFlowUuid()));
//        Map<String, ExecutionPlan> dependencies = new HashMap<>();
//        dependencies.put(subFlowExecutionPlan.getFlowUuid(), subFlowExecutionPlan);
//
//        TriggeringProperties triggeringProperties = TriggeringProperties.create(executionPlan).setDependencies(dependencies);
//        registerEventListener(EventConstants.SCORE_FINISHED_EVENT, SessionDataActions.SESSION_BEFORE_PUT_DATA_EVENT, SessionDataActions.SESSION_GET_DATA_EVENT);
//        score.trigger(triggeringProperties);
//
//        waitForAllEventsToArrive(3);
//        ScoreEvent sessionBeforePutEvent = getEventFromQueueByType(SessionDataActions.SESSION_BEFORE_PUT_DATA_EVENT);
//        Assert.assertEquals(sessionBeforePutEvent.getData(), null);
//        ScoreEvent sessionGetEvent = getEventFromQueueByType(SessionDataActions.SESSION_GET_DATA_EVENT);
//        Assert.assertEquals(sessionGetEvent.getData(), SessionDataActions.TEST_VALUE);
//    }

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

    private static ExecutionPlan createExecutionPlan() {
        ExecutionPlan executionPlan = new ExecutionPlan();

        executionPlan.setFlowUuid("1");

        executionPlan.setBeginStep(0L);

        ExecutionStep executionStep = createExecutionStep(0L, "org.score.samples.controlactions.ConsoleControlActions", "echoHelloScore", new HashMap<String, Serializable>());
        addNavigationToExecutionStep(1L, navigationActionClassName, simpleNavigationMethodName, executionStep);
        executionPlan.addStep(executionStep);

        ExecutionStep executionStep2 = createExecutionStep(1L, "org.score.samples.controlactions.ConsoleControlActions", "echoHelloScore", new HashMap<String, Serializable>());
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
        ExecutionStep executionSplitStep = createExecutionStep(0L, "org.score.samples.controlactions.BranchActions", "split", actionData);
        executionSplitStep.setSplitStep(true);
        addNavigationToExecutionStep(1L, navigationActionClassName, simpleNavigationMethodName, executionSplitStep);
        executionPlan.addStep(executionSplitStep);

        ExecutionStep executionStep2 = createExecutionStep(1L, "org.score.samples.controlactions.BranchActions", "join", new HashMap<String, Serializable>());
        addNavigationToExecutionStep(2L, navigationActionClassName, simpleNavigationMethodName, executionStep2);
        executionPlan.addStep(executionStep2);

        ExecutionStep executionStep3 = createExecutionStep(2L, "org.score.samples.controlactions.ConsoleControlActions", "echoHelloScore", actionData);
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
        ExecutionStep executionSplitStep = createExecutionStep(0L, "org.score.samples.controlactions.BranchActions", "parallelSplit", actionData);
        executionSplitStep.setSplitStep(true);
        addNavigationToExecutionStep(1L, navigationActionClassName, simpleNavigationMethodName, executionSplitStep);
        executionPlan.addStep(executionSplitStep);

        ExecutionStep executionStep2 = createExecutionStep(1L, "org.score.samples.controlactions.BranchActions", "join", new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep2);

        return executionPlan;
    }

    private static ExecutionPlan createSessionDataExecutionPlan() {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setFlowUuid("basicSessionDataFlow");
        executionPlan.setBeginStep(0L);

        ExecutionStep executionPutDataStep = createExecutionStep(0L, "org.score.samples.controlactions.SessionDataActions", "putObject", new HashMap<String, Serializable>());
        addNavigationToExecutionStep(1L, navigationActionClassName, simpleNavigationMethodName, executionPutDataStep);
        executionPlan.addStep(executionPutDataStep);

//        ExecutionStep sleepDataStep = createExecutionStep(1L, "org.score.samples.controlactions.SessionDataActions", "sleepAction");
//        addNavigationToExecutionStep(2L, simpleNavigationMethodName, navigationActionClassName, sleepDataStep);
//        executionPlan.addStep(sleepDataStep);

        ExecutionStep executionGetDataStep = createExecutionStep(1L, "org.score.samples.controlactions.SessionDataActions", "getObject", new HashMap<String, Serializable>());
        executionPlan.addStep(executionGetDataStep);
        return executionPlan;
    }

    private ExecutionPlan createParentPutOnSessionExecutionPlan(String childFlowId) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setFlowUuid("parentPutOnSessionFlow");
        executionPlan.setBeginStep(0L);

        ExecutionStep executionPutDataStep = createExecutionStep(0L, "org.score.samples.controlactions.SessionDataActions", "putObject", new HashMap<String, Serializable>());
        addNavigationToExecutionStep(1L, navigationActionClassName, simpleNavigationMethodName, executionPutDataStep);
        executionPlan.addStep(executionPutDataStep);

        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(BranchActions.STEP_POSITION, 1L);
        actionData.put(BranchActions.EXECUTION_PLAN_ID, childFlowId);

        ExecutionStep executionSplitStep = createExecutionStep(1L, "org.score.samples.controlactions.BranchActions", "split", actionData);
        executionSplitStep.setSplitStep(true);
        addNavigationToExecutionStep(2L, navigationActionClassName, simpleNavigationMethodName, executionSplitStep);
        executionPlan.addStep(executionSplitStep);

        ExecutionStep executionJoinStep = createExecutionStep(2L, "org.score.samples.controlactions.BranchActions", "join", new HashMap<String, Serializable>());
        executionPlan.addStep(executionJoinStep);

        return executionPlan;
    }

    private ExecutionPlan createChildGetFromSessionExecutionPlan(){

        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setFlowUuid("childGetFromSessionFlow");
        executionPlan.setBeginStep(0L);

        ExecutionStep executionGetDataStep = createExecutionStep(0L, "org.score.samples.controlactions.SessionDataActions", "getObject",new HashMap<String, Serializable>());
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
