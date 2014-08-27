package com.hp.score.worker.management.services;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 8/7/14
 * Time: 2:26 PM
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
