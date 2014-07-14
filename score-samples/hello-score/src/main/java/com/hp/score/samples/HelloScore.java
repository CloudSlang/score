package com.hp.score.samples;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.api.Score;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

        ExecutionStep executionStep = new ExecutionStep(0L);
        executionPlan.addStep(executionStep);

        return executionPlan;
    }

}
