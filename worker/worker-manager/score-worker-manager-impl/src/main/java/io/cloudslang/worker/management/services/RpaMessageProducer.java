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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

public class RpaMessageProducer {
    private static final Logger logger = Logger.getLogger(RpaMessageProducer.class);

    private static final String EXCHANGE_NAME = "topic_rpa";
    private static final String TOPIC = "topic";
    private static ConnectionFactory factory;
    private static Connection connection;
    private static final String ROUTING_KEY = "rpa.routing.key";
    private static GenericObjectPool<Channel> channelPool;

    @PostConstruct
    private void init() throws Exception {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();

        channelPool = new GenericObjectPool<>(getChannelPoolableObjectFactory());
        channelPool.addObject();
        channelPool.addObject();
        channelPool.addObject();
        channelPool.addObject();
        channelPool.addObject();
    }

    @PreDestroy
    private void destruct() throws IOException {
        //TODO close all stuff (i.e. channels)
        connection.close();
    }

    public void produce(ExecutionMessage executionMessage) {
        try {
            Channel channel = channelPool.borrowObject();
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, SerializationUtils.serialize(executionMessage));
        } catch (Exception e) {
            // TODO
            logger.error(e);
        }
        // TODO change to debug or remove log
        logger.error(" [x] Sent '" + ROUTING_KEY + "':'" + executionMessage.getMsgId() + "'");
    }

    private PoolableObjectFactory<Channel> getChannelPoolableObjectFactory() {
        return new PoolableObjectFactory<Channel>() {
                @Override
                public Channel makeObject() throws Exception {
                    Channel channel = connection.createChannel();
                    channel.exchangeDeclare(EXCHANGE_NAME, TOPIC);
                    return channel;
                }

                @Override
                public void destroyObject(Channel obj) throws Exception {
                    //obj.close();
                }

                @Override
                public boolean validateObject(Channel obj) {
                    return obj.isOpen();
                }

                @Override
                public void activateObject(Channel obj) throws Exception {
                    obj = connection.createChannel();
                    obj.exchangeDeclare(EXCHANGE_NAME, TOPIC);
                }

                @Override
                public void passivateObject(Channel obj) throws Exception {
                    //obj.abort();
                }
            };
    }

}
