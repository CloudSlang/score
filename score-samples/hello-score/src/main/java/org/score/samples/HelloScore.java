package org.score.samples;

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
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
 * User: maromg
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
