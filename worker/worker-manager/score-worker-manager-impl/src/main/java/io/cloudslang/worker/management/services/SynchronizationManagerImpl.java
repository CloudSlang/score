/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.worker.management.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizationManagerImpl implements SynchronizationManager {
    private static final Logger logger = LogManager.getLogger(SynchronizationManagerImpl.class);

    private final ReentrantLock recoveryGetLock = new ReentrantLock();  //synchronizing Recovery and InBuffer
    private final ReentrantLock recoveryPutLock = new ReentrantLock(); //synchronizing Recovery and OutBuffer put()
    private final ReentrantLock recoveryDrainLock = new ReentrantLock(); //synchronizing Recovery and OutBuffer drain()
    private final ReentrantLock outBufferLock = new ReentrantLock();   //synchronizing OutBuffer put() and drain()

    private final Condition notEmpty = outBufferLock.newCondition();
    private final Condition notFull = outBufferLock.newCondition();

    @Override
    public void startRecovery() {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting recovery locks...");
        }

        recoveryGetLock.lock();
        recoveryPutLock.lock();
        recoveryDrainLock.lock();
        outBufferLock.lock();

        if (logger.isDebugEnabled()) {
            logger.debug("Got recovery locks...");
        }
    }

    @Override
    public void finishRecovery() {
        if (logger.isDebugEnabled()) {
            logger.debug("Releasing recovery locks...");
        }

        //we cleaned buffers - need to signal for those who wait for the buffer to be not full
        notFull.signalAll();

        recoveryGetLock.unlock();
        recoveryPutLock.unlock();
        recoveryDrainLock.unlock();
        outBufferLock.unlock();

        if (logger.isDebugEnabled()) {
            logger.debug("Released recovery locks...");
        }
    }

    @Override
    public void startPutMessages() {
        recoveryPutLock.lock();
        outBufferLock.lock();
    }

    @Override
    public void finishPutMessages() {
        notEmpty.signalAll();
        unlockCompletely(recoveryPutLock);
        unlockCompletely(outBufferLock); // probably just unlock is enough
    }

    @Override
    public void startDrain() {
        if (logger.isDebugEnabled()) {
            logger.debug("In SynchronizationManager.startDrain()");
        }
        recoveryDrainLock.lock();
        outBufferLock.lock();

        if (logger.isDebugEnabled()) {
            logger.debug("Out SynchronizationManager.startDrain()");
        }
    }

    @Override
    public void finishDrain() {
        if (logger.isDebugEnabled()) {
            logger.debug("In SynchronizationManager.finishDrain()");
        }

        notFull.signalAll();

        unlockCompletely(recoveryDrainLock);
        unlockCompletely(outBufferLock); //probably just unlock is enough

        if (logger.isDebugEnabled()) {
            logger.debug("Out SynchronizationManager.finishDrain()");
        }
    }

    @Override
    public void waitForDrain() throws InterruptedException {
        if (logger.isDebugEnabled()) {
            logger.debug("In SynchronizationManager.waitForDrain()");
        }

        //unlock all recovery locks that could be taken - so when we are waiting for messages drain recovery will be able to begin
        unlockCompletely(recoveryPutLock);
        unlockCompletely(recoveryGetLock); //if we got here from the InBuffer thread

        //Wait for messages to be drained
        notFull.await();

        if (logger.isDebugEnabled()) {
            logger.debug("Out SynchronizationManager.waitForDrain()");
        }

    }

    @Override
    public void waitForMessages() throws InterruptedException {
        if (logger.isDebugEnabled()) {
            logger.debug("In SynchronizationManager.waitForMessages()");
        }

        //unlock recovery so when we are waiting for messages recovery will be able to begin
        unlockCompletely(recoveryDrainLock);
        //Wait for messages to arrive
        notEmpty.await();

        if (logger.isDebugEnabled()) {
            logger.debug("Out SynchronizationManager.waitForMessages()");
        }
    }

    @Override
    public void startGetMessages() {
        if (logger.isDebugEnabled()) {
            logger.debug("In SynchronizationManager.startGetMessages()");
        }

        recoveryGetLock.lock();

        if (logger.isDebugEnabled()) {
            logger.debug("Out SynchronizationManager.startGetMessages()");
        }
    }

    @Override
    public void finishGetMessages() {
        if (logger.isDebugEnabled()) {
            logger.debug("In SynchronizationManager.finishGetMessages()");
        }

        unlockCompletely(recoveryGetLock);

        if (logger.isDebugEnabled()) {
            logger.debug("Out SynchronizationManager.finishGetMessages()");
        }
    }

    // It is very important to use this method instead of just do unlock because of 2 reasons:
    // 1. The lock could be already unlocked in case out thread was in waitForMessages() or waitForDrain()
    //2. The lock can be locked more then once - for example in InBuffer startGetMessages() and then in ackMessages() do put() in OutBuffer and do startPutMessages()
    private void unlockCompletely(ReentrantLock lockToUnlock) {
        int counter = lockToUnlock.getHoldCount();

        for (int i = 0; i < counter; i++) {
            lockToUnlock.unlock();
        }
    }
}
