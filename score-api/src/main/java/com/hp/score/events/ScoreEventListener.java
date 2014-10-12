package com.hp.score.events;

/**
 * Created with IntelliJ IDEA.
 * User: Amit Levin
 * Date: 09/01/14
 * To change this template use File | Settings | File Templates.
 */
public interface ScoreEventListener {

    /**
     * handler of score event, this method will be called on score event
     * @param event - the event that dispatched
     */
	void onEvent(ScoreEvent event);

}
