package com.hp.score.samples.controlactions;

/**
 * User: stoneo
 * Date: 20/07/2014
 * Time: 17:36
 */
public class NavigationActions {

    private static long nextStep = 0;

    public long nextStepNavigation(){
        nextStep++;
        return nextStep;
    }
}
