package com.hp.score.samples;

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.api.Score;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.util.HashMap;

/**
 * User: maromg
 * Date: 22/06/2014
 */
public class HelloScore {

    public static void main(String[] args) {
        ApplicationContext context = loadScore();
        ExecutionPlan executionPlan = createExecutionPlan();
        Score score = context.getBean(Score.class);
        score.trigger(executionPlan);
    }

    private static ApplicationContext loadScore() {
        return new ClassPathXmlApplicationContext("/META-INF/spring/helloScoreContext.xml");
    }

    private static ExecutionPlan createExecutionPlan() {
        ExecutionPlan executionPlan = new ExecutionPlan();

        executionPlan.setFlowUuid("1");

        executionPlan.setBeginStep(0L);

        ExecutionStep executionStep = new ExecutionStep(0L);
        executionStep.setAction(new ControlActionMetadata("com.hp.score.samples.controlactions.ConsoleControlActions", "echoHelloScore"));
        executionStep.setActionData(new HashMap<String, Serializable>());
        executionStep.setNavigation(new ControlActionMetadata("com.hp.score.samples.controlactions.NavigationActions", "nextStepNavigation"));
        executionStep.setNavigationData(new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep);

        ExecutionStep executionStep2 = new ExecutionStep(1L);
        executionStep2.setAction(new ControlActionMetadata("com.hp.score.samples.controlactions.ConsoleControlActions", "echoHelloScore"));
        executionStep2.setActionData(new HashMap<String, Serializable>());

        executionPlan.addStep(executionStep2);

        return executionPlan;
    }

}
