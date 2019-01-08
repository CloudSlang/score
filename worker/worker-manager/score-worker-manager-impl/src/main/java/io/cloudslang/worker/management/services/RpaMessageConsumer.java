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
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.orchestrator.entities.MessageType;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;

public class RpaMessageConsumer implements SmartLifecycle {
    private static final Logger logger = Logger.getLogger(RpaMessageConsumer.class);

    // TODO get all properties from installer/property file
    private static final String EXCHANGE_NAME = "topic_rpa";
    private static final String TOPIC = "topic";
    private static final String ROUTING_KEY = "rpa.routing.key.rasx.out";
    private static final String HOST = "localhost";

    private static ConnectionFactory factory;
    private static Connection connection;
    private static GenericObjectPool<Channel> channelPool;

    @Autowired
    private RpaConnectionPoolFactory rpaConnectionPoolFactory;
    @Autowired
    private OutboundBuffer outBuffer;

    @PostConstruct
    private void init() throws Exception {
        factory = new ConnectionFactory();
        factory.setHost(HOST);
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
                    try {
                        outBuffer.put(getExecutionMessage(body));
                    } catch (InterruptedException e) {
                        logger.error(e);
                    } catch (ParserConfigurationException e) {
                        logger.error(e);
                    } catch (IOException e) {
                        logger.error(e);
                    } catch (SAXException e) {
                        logger.error(e);
                    } catch (XPathExpressionException e) {
                        logger.error(e);
                    }
                }
            };
            channel.basicConsume(queueName, true, consumer);
            channelPool.returnObject(channel);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private ExecutionMessage getExecutionMessage(byte[] body) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        String result = getResult(new String(body));
        ExecutionMessage executionMessage = new ExecutionMessage();
        executionMessage.setPayload(new Payload(body));
        executionMessage.setStatus(getExecStatus(result));
        executionMessage.setMessageType(MessageType.RPA_OUT);
        return executionMessage;
    }

    private ExecStatus getExecStatus(String result) {
        // TODO check result types
        switch (result) {
            case "Failure": return ExecStatus.FAILED;
            case "Done": return ExecStatus.FINISHED;
            case "Warning": return ExecStatus.FAILED;
            default: return null;
        }
    }

    private static String getResult(String xml) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("/Results/ReportNode[@type='testrun']/Data/Result");

        return (String) expr.evaluate(document, XPathConstants.STRING);
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
