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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;

public class RpaMessageConsumer implements SmartLifecycle {
    private static final Logger logger = Logger.getLogger(RpaMessageConsumer.class);

    private static final String EXCHANGE_NAME = "topic_rpa";
    private static final String TOPIC = "topic";
    private static final String ROUTING_KEY = "rpa.routing.key";
    private static ConnectionFactory factory;
    private static Connection connection;
    private static GenericObjectPool<Channel> channelPool;

    @Autowired
    private ItpaMessageHandler itpaMessageHandler;
    @Autowired
    private RpaConnectionPoolFactory rpaConnectionPoolFactory;

    @PostConstruct
    private void init() throws Exception {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();

        channelPool = new GenericObjectPool<>(rpaConnectionPoolFactory
                .getChannelPoolableObjectFactory(connection, EXCHANGE_NAME, TOPIC));
        channelPool.addObject();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable runnable) {
        try {
            channelPool.close();
            connection.close();
        } catch (IOException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void start() {
        try {
            Channel channel = channelPool.borrowObject();
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, ROUTING_KEY);
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) {
                    ExecutionMessage executionMessage = (ExecutionMessage) SerializationUtils.deserialize(body);
                    itpaMessageHandler.handle(executionMessage);
                }
            };
            channel.basicConsume(queueName, true, consumer);
            channelPool.returnObject(channel);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void stop() {
        //TODO return to pool
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public int getPhase() {
        return 0;
    }
}
