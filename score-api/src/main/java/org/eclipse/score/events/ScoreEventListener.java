/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.events;

/**
 * Created with IntelliJ IDEA.
 * User:
 * Date: 09/01/14
 * To change this template use File | Settings | File Templates.
 */
public interface ScoreEventListener {

    /**
     * handler of score event, this method will be called on score event
     * @param event - the event that dispatched
     */
	void onEvent(ScoreEvent event) throws InterruptedException ;

}
