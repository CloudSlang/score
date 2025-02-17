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

import io.cloudslang.orchestrator.entities.Message;
import io.cloudslang.orchestrator.services.OrchestratorDispatcherService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class OutboundBufferTest {

    private final Logger logger = LogManager.getLogger(getClass());

    private static final int MAX_BUFFER_WEIGHT = 10;
    private static final int MAX_BULK_WEIGHT = 3;

    @Autowired
    private WorkerRecoveryManager recoveryManager;

    @Autowired
    private OutboundBuffer buffer;

    @Autowired
    private OrchestratorDispatcherService dispatcherService;

    @Before
    public void setUp() {
        ((WorkerRecoveryListener) buffer).doRecovery();
        reset(recoveryManager, dispatcherService);
    }

    /**
     * Makes sure the buffer aggregates messages and dispatches them in bulk
     */
    @Test
    public void testAggregation() throws InterruptedException {
        List<DummyMsg1> messages = Arrays.asList(new DummyMsg1(), new DummyMsg1());

        for (DummyMsg1 message : messages) {
            buffer.put(message);
        }

        buffer.drain();
        verify(dispatcherService)
                .dispatch((List<? extends Serializable>) argThat(new MessagesSizeMatcher(messages)), anyString(),
                        any(), anyString());
    }

    /**
     * checks that when inserting messages to a full buffer, the inserting thread will block until the buffer is
     * emptied
     */
    @Test
    public void testProducerBlocking() throws InterruptedException {
        // buffer capacity is 10, put messages in it until it is full
        while (buffer.getWeight() < MAX_BUFFER_WEIGHT) {
            buffer.put(new DummyMsg1());
        }

        // the next insert to buffer will block because it's full, do it on a different thread
        Thread thread = new Thread(() -> {
			try {
				buffer.put(new DummyMsg1());
			} catch (InterruptedException e) {
				//ignore
			}
		});
        thread.start();

        // wait for that thread to block
        waitForThreadStateToBe(thread, Thread.State.WAITING);
        assertEquals("inserting thread should be in a waiting state when inserting to full buffer",
                Thread.State.WAITING, thread.getState());

        // drain the buffer -> will send the first 10 messages and release the blocking thread
        buffer.drain();
        waitForThreadStateToBe(thread, Thread.State.TERMINATED);
        assertEquals("inserting thread should be in a terminated state after inserting to buffer",
                Thread.State.TERMINATED, thread.getState());
        thread.join();
    }

    /**
     * Checks that the buffer will block when having no messages and continues when the first message arrives
     */
    @Test
    public void testConsumerBlocking() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                buffer.drain();
            }
        });
        thread.start();

        // draining the buffer should block since it is empty
        waitForThreadStateToBe(thread, Thread.State.WAITING);
        assertEquals("reading thread should be in a waiting state when inserting to full buffer", Thread.State.WAITING,
                thread.getState());

        // insert 2 new messages
        Message[] messages = new Message[]{new DummyMsg1(), new DummyMsg2()};
        buffer.put(messages);

        // thread should be released now
        waitForThreadStateToBe(thread, Thread.State.TERMINATED);
        assertEquals("reading thread should be in a terminated after a message was inserted to the buffer",
                Thread.State.TERMINATED, thread.getState());

        thread.join();
        verify(dispatcherService)
                .dispatch((List<? extends Serializable>) argThat(new MessagesSizeMatcher(Arrays.asList(messages))),
                        anyString(), any(), anyString());
    }


    private void waitForThreadStateToBe(Thread thread, Thread.State state) throws InterruptedException {
        int waitCount = 0;
        while (!thread.getState().equals(state) && waitCount <= 20) {
            Thread.sleep(50);
            waitCount++;
        }
    }

    @Test
    public void longevityTest() throws InterruptedException {
        int THREADS_NUM = 5;
        long CHECK_DURATION = 5 * 1000L;
        long INFO_FREQUENCY = 2 * 1000L;

        final AtomicBoolean run = new AtomicBoolean(true);
        final CountDownLatch latch = new CountDownLatch(THREADS_NUM + 1);

        for (int i = 1; i <= THREADS_NUM; i++) {
            final int index = i;
            new Thread(new Runnable() {
                private final Class<? extends Message> messageClass =
                        (index % 2) != 0 ? DummyMsg1.class : DummyMsg2.class;

                @Override
                public void run() {
                    int counter = 0;
                    try {
                        logger.debug("started, will generate messages of " + messageClass.getSimpleName());

                        while (run.get()) {
                            buffer.put(messageClass.newInstance());
                            counter++;
                            Thread.sleep(5L);
                        }
                        logger.debug("thread finished. processed " + counter + " messages");
                    } catch (Exception ex) {
                        logger.error("thread finished", ex);
                    } finally {
                        latch.countDown();
                    }
                }
            }, "T-" + i).start();
        }

        final DrainStatistics statistics = new DrainStatistics();
        //noinspection unchecked
        doAnswer((Answer<Object>) invocation -> {
			@SuppressWarnings("unchecked") List<Message> messages = (List<Message>) invocation.getArguments()[0];
			int weight = 0;
			for (Message message : messages) {
				weight += message.getWeight();
			}
			statistics.add(messages.size(), weight);
			return null;
		}).when(dispatcherService).dispatch(anyList(), anyString(), any(), anyString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.debug("started");

                    while (run.get()) {
                        buffer.drain();
                        Thread.sleep(30L);
                    }

					while (buffer.getSize() > 0) {
						buffer.drain();
					}
                } catch (Exception ex) {
                    logger.error("thread finished", ex);
                } finally {
                    latch.countDown();
                }
            }
        }, "T-D").start();

        long t = System.currentTimeMillis();
        while (System.currentTimeMillis() - t < CHECK_DURATION) {
            Thread.sleep(INFO_FREQUENCY);
            logger.debug(buffer.getStatus());
        }
        run.set(false);
        latch.await();

        System.out.println("Drain statistics: " + statistics.report());
    }


    /**
     * Makes sure the recovery clears worker state
     */
    @Test
    public void testRecovery() throws InterruptedException {
        List<DummyMsg1> messages = Arrays.asList(new DummyMsg1(), new DummyMsg1());

        for (DummyMsg1 message : messages) {
            buffer.put(message);
        }
        assertEquals(1, buffer.getSize());
        assertEquals(2, buffer.getWeight());
        ((WorkerRecoveryListener) buffer).doRecovery();
        assertEquals(0, buffer.getSize());
        assertEquals(0, buffer.getWeight());
    }

    private class MessagesSizeMatcher implements ArgumentMatcher {

        List messages;

        public MessagesSizeMatcher(List val) {
            messages = val;
        }

        @Override
        public boolean matches(Object argument) {
            if (argument instanceof List) {
                List listConverted = (List) argument;
                if (listConverted.size() == messages.size()) {
                    return true;
                }
            }
            return false;
        }
    }

    static class DrainStatistics {

        private int counter;
        private int size;
        private int weight;

        public void add(int size, int weight) {
            counter++;
            this.size += size;
            this.weight += weight;
        }

        public String report() {
            return "Buffer has sent " + counter + " bulks, avg(size): " + size / counter + ", avg(weight): "
                    + weight / counter + ", total messages: " + size;
        }
    }


    static class DummyMsg1 implements Message {

        public int getWeight() {
            return 1;
        }

        public String getId() {
            return "";
        }

        public List<Message> shrink(List<Message> messages) {
            return messages;
        }
    }

    static class DummyMsg2 implements Message {

        public int getWeight() {
            return 2;
        }

        public String getId() {
            return "";
        }

        public List<Message> shrink(List<Message> messages) {
            return messages;
        }
    }

    @Configuration
    static class config {

        static {
            System.setProperty("out.buffer.max.buffer.weight", String.valueOf(MAX_BUFFER_WEIGHT));
            System.setProperty("out.buffer.max.bulk.weight", String.valueOf(MAX_BULK_WEIGHT));
        }

        @Bean
        public WorkerRecoveryManager workerRecoveryManager() {
            return mock(WorkerRecoveryManager.class);
        }

        @Bean
        OrchestratorDispatcherService orchestratorDispatcherService() {
            return mock(OrchestratorDispatcherService.class);
        }

        @Bean
        SynchronizationManager synchronizationManager() {
            return new SynchronizationManagerImpl();
        }

        @Bean
        public RetryTemplate retryTemplate() {
            return new RetryTemplate();
        }

        @Bean
        public OutboundBuffer outboundBuffer() {
            return new OutboundBufferImpl();
        }

        @Bean
        public String workerUuid() {
            return "1234";
        }

        @Bean
        public Integer numberOfExecutionThreads() {
            return 10;
        }

    }

}
