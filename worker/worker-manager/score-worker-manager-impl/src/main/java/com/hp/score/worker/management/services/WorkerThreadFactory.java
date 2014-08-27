package com.hp.score.worker.management.services;

import java.util.concurrent.ThreadFactory;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 25/11/12
 * Time: 09:01
 */
class WorkerThreadFactory implements ThreadFactory {

    private int index;
    private String name;

    public WorkerThreadFactory(String commonName) {
        name = commonName;
    }

    public Thread newThread(final Runnable command) {
        return new Thread(new Runnable() {
            public void run() {
                command.run();
            }
        }, name + "-" + getThreadIDX());
    }

    private int getThreadIDX() {
        int idx;
        synchronized (this) {
            idx = index++;
        }
        return idx;
    }
}
