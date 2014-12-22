/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.worker.management.services;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 8/7/14
 * Time: 2:26 PM
 *
 * This manager is responsible to synchronize all workers activities: drain/poll/recovery
 */
public interface SynchronizationManager {

    void startRecovery();
    void finishRecovery();

    void startPutMessages();
    void finishPutMessages();

    void startGetMessages();
    void finishGetMessages();

    void waitForMessages() throws InterruptedException;
    void waitForDrain() throws InterruptedException;

    void startDrain();
    void finishDrain();
}
