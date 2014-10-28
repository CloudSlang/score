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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * User: wahnonm
 * Date: 12/08/13
 * Time: 10:10
 */
public class WorkerThreadFactoryTest {

    List<Long> endedThreads ;

    @Before
    public void setUp(){
        endedThreads = Collections.synchronizedList(new ArrayList<Long>());
    }


    public class simpleCmd implements Runnable{

        private Long id;

        public simpleCmd(Long id){
           this.id = id;
        }

        @Override
        public void run() {
            System.out.println("Just Msg");
            endedThreads.add(id);
        }
    }

    @Test
    public void testNewThread() throws Exception {
        Assert.assertTrue(endedThreads.isEmpty());

        WorkerThreadFactory factory = new WorkerThreadFactory("worker1");
        Thread thread1 = factory.newThread(new simpleCmd(1L));
        Thread thread2 = factory.newThread(new simpleCmd(2L));

        Assert.assertNotSame(thread1.getName(),thread2.getName());

        thread1.start();
        thread2.start();

        while(thread1.isAlive() || thread2.isAlive()){}

        Assert.assertEquals(2,endedThreads.size());
        Assert.assertEquals(3,endedThreads.get(0)+endedThreads.get(1));
    }
}
