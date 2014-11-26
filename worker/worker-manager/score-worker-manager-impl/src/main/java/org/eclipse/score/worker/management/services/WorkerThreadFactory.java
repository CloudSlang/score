/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.worker.management.services;

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
