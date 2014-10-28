/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.worker.management.services;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 8/7/14
 * Time: 2:30 PM
 */
public class SynchronizationManagerImpl implements SynchronizationManager {

    ReentrantLock recoveryGetLock = new ReentrantLock();
    ReentrantLock recoveryPutLock = new ReentrantLock();
    ReentrantLock recoveryDrainLock = new ReentrantLock();

    ReentrantLock outBufferLock = new ReentrantLock();
    private final Condition notEmpty = outBufferLock.newCondition();
    private final Condition notFull = outBufferLock.newCondition();


    @Override
    public void startRecovery() {
        recoveryGetLock.lock();
        recoveryPutLock.lock();
        recoveryDrainLock.lock();

        outBufferLock.lock();
    }

    @Override
    public void finishRecovery(){
        //we cleaned buffers - need to signal for those who wait for the buffer to be not full
        notFull.signalAll();

        recoveryGetLock.unlock();
        recoveryPutLock.unlock();
        recoveryDrainLock.unlock();

        outBufferLock.unlock();
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
        unlockCompletely(outBufferLock); //probably just unlock is enough
    }

    @Override
    public void startDrain() {
        recoveryDrainLock.lock();
        outBufferLock.lock();
    }

    @Override
    public void finishDrain() {
        notFull.signalAll();

        unlockCompletely(recoveryDrainLock);
        unlockCompletely(outBufferLock); //probably just unlock is enough
    }

    @Override
    public void waitForDrain() throws InterruptedException {
        //unlock all recovery locks that could be taken - so when we are waiting for messages drain recovery will be able to begin
        unlockCompletely(recoveryPutLock);
        unlockCompletely(recoveryGetLock); //if we got here from the InBuffer thread

        //Wait for messages to be drained
        notFull.await();
    }

    @Override
    public void waitForMessages() throws InterruptedException {
        //unlock recovery so when we are waiting for messages recovery will be able to begin
        unlockCompletely(recoveryDrainLock);
        //Wait for messages to arrive
        notEmpty.await();
    }

    @Override
    public void startGetMessages() {
        recoveryGetLock.lock();
    }

    @Override
    public void finishGetMessages() {
        unlockCompletely(recoveryGetLock);
    }

    // It is very important to use this method instead of just do unlock because of 2 reasons:
    // 1. The lock could be already unlocked in case out thread was in waitForMessages() or waitForDrain()
    //2. The lock can be locked more then once - for example in InBuffer startGetMessages() and then in ackMessages() do put() in OutBuffer and do startPutMessages()
    private void unlockCompletely(ReentrantLock lockToUnlock){
        int counter = lockToUnlock.getHoldCount();

        for(int i = 0; i<counter; i++){
            lockToUnlock.unlock();
        }
    }
}
