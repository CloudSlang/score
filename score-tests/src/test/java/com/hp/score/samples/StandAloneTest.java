package com.hp.score.samples;

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
import java.util.Map;
import java.util.Set;
import java.util.List;

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

    private List<Serializable> eventQueue = Collections.synchronizedList(new ArrayList<Serializable>());

    private final static Logger logger = Logger.getLogger(StandAloneTest.class);

    @Before
    public void init(){
        eventQueue = new ArrayList<Serializable>();
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
        long finishEventExecutionId = (Long)((Map)eventQueue.get(0)).get(ExecutionConstants.EXECUTION_ID_CONTEXT);
        Assert.assertNotNull(finishEventExecutionId);
        Assert.assertEquals(executionId,finishEventExecutionId);
    }

    @Test(timeout = 20000)
    public void subFlowTest() {
        ExecutionPlan executionPlan = createFlowWithSubflowExecutionPlan();
        ExecutionPlan subFlowExecutionPlan = createExecutionPlan();
        executionPlan.setSubflowsUUIDs(Sets.newHashSet(subFlowExecutionPlan.getFlowUuid()));
        TriggeringProperties triggeringProperties = TriggeringProperties.create(executionPlan);
        triggeringProperties.getDependencies().put(subFlowExecutionPlan.getFlowUuid(),subFlowExecutionPlan);
        Map<String,Serializable> getRuntimeValues = new HashMap<String, Serializable>();
        getRuntimeValues.put("NEW_BRANCH_MECHANISM",Boolean.TRUE);//TODO - remove this !! needs to work with this on by default, pending Non-Blocking story
        triggeringProperties.setRuntimeValues(getRuntimeValues);
        registerEventListener("Hello score");

        score.trigger(triggeringProperties);

        waitForAllEventsToArrive(2);//this flow should have 2 "Hello score" events only
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

        ExecutionStep executionStep = new ExecutionStep(0L);
        executionStep.setAction(new ControlActionMetadata("org.score.samples.controlactions.ConsoleControlActions", "echoHelloScore"));
        executionStep.setActionData(new HashMap<String, Serializable>());
        executionStep.setNavigation(new ControlActionMetadata("org.score.samples.controlactions.NavigationActions", "nextStepNavigation"));
        executionStep.setNavigationData(new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep);

        ExecutionStep executionStep2 = new ExecutionStep(1L);
        executionStep2.setAction(new ControlActionMetadata("org.score.samples.controlactions.ConsoleControlActions", "echoHelloScore"));
        executionStep2.setActionData(new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep2);

        return executionPlan;
    }

    private static ExecutionPlan createFlowWithSubflowExecutionPlan() {
        ExecutionPlan executionPlan = new ExecutionPlan();

        executionPlan.setFlowUuid("parentFlow");

        executionPlan.setBeginStep(0L);

        ExecutionStep executionSplitStep = new ExecutionStep(0L);
        executionSplitStep.setSplitStep(true);
        executionSplitStep.setAction(new ControlActionMetadata("org.score.samples.controlactions.BranchActions", "split"));
        executionSplitStep.setActionData(new HashMap<String, Serializable>());
        executionSplitStep.setNavigation(new ControlActionMetadata("org.score.samples.controlactions.NavigationActions", "simpleNavigation"));
        Map<String, Serializable> navigationData = new HashMap<String, Serializable>();
        navigationData.put("nextStepId",1L);
        executionSplitStep.setNavigationData(navigationData);

        executionPlan.addStep(executionSplitStep);

        ExecutionStep executionStep2 = new ExecutionStep(1L);
        executionStep2.setAction(new ControlActionMetadata("org.score.samples.controlactions.BranchActions", "join"));
        executionStep2.setActionData(new HashMap<String, Serializable>());

        executionStep2.setNavigation(new ControlActionMetadata("org.score.samples.controlactions.NavigationActions", "simpleNavigation"));
        navigationData.put("nextStepId", 2L);
        executionStep2.setNavigationData(navigationData);

        executionPlan.addStep(executionStep2);

        ExecutionStep executionStep3 = new ExecutionStep(2L);
        executionStep3.setAction(new ControlActionMetadata("org.score.samples.controlactions.ConsoleControlActions", "echoHelloScore"));
        executionStep3.setActionData(new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep3);

        return executionPlan;
    }

    private void registerEventListener(String eventType) {
        Set<String> handlerTypes = new HashSet();
        handlerTypes.add(eventType);
        eventBus.subscribe(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                logger.info("Listener " + this.toString() + " invoked on type: " + event.getEventType() + " with data: " + event.getData());
                eventQueue.add(event.getData());
            }
        }, handlerTypes);
    }
}
