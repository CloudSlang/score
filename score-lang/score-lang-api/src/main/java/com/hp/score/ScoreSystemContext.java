package com.hp.score;

import java.io.Serializable;
import java.util.Map;

/**
 * User: maromg
 * Date: 10/06/2014
 *
 * The ScoreSystemContext class provides runtime services for control actions.
 */
public interface ScoreSystemContext extends Map<String, Serializable> {

    /**
     * Adds an event to the system context. The event will not be processed until after the calling control action
     * has finished running.
     * @param type the type of the event
     * @param data the data contained in the event
     */
    void addEvent(String type, Serializable data);

    /**
     * Requests to pause the current execution.
     */
    void pauseExecution();

    /**
     * Sets an error on the system context, signaling that the current execution had an error and should not continue
     * @param error the error to set
     */
    void setError(String error);


}
