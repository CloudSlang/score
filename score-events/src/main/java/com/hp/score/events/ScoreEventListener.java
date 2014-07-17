package com.hp.score.events;

import com.hp.score.api.ScoreEvent;

/**
 * Created with IntelliJ IDEA.
 * User: Amit Levin
 * Date: 09/01/14
 * To change this template use File | Settings | File Templates.
 */
public interface ScoreEventListener {

	void onEvent(ScoreEvent event);

}
