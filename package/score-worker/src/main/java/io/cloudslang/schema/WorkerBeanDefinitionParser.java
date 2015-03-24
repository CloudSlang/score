/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.schema;

import io.cloudslang.events.EventBusImpl;
import io.cloudslang.worker.execution.reflection.ReflectionAdapterImpl;
import io.cloudslang.worker.execution.services.ExecutionServiceImpl;
import io.cloudslang.worker.execution.services.SessionDataHandlerImpl;
import io.cloudslang.worker.management.WorkerConfigurationServiceImpl;
import io.cloudslang.worker.management.WorkerRegistration;
import io.cloudslang.worker.management.monitor.ScheduledWorkerLoadMonitor;
import io.cloudslang.worker.management.monitor.WorkerMonitorsImpl;
import io.cloudslang.worker.management.services.*;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @since 21/01/2014
 */
public class WorkerBeanDefinitionParser extends AbstractBeanDefinitionParser {

	private Map<Class<?>,String> beans = new HashMap<Class<?>,String>(){{
		put(WorkerManager.class, "workerManager");
		put(EventBusImpl.class, null);
		put(ExecutionServiceImpl.class, "agent");
		put(InBuffer.class, null);
		put(OutboundBufferImpl.class, "outBuffer");
		put(RetryTemplate.class, null);
		put(SimpleExecutionRunnableFactory.class, null);
		put(WorkerManagerMBean.class, "org.openscore.worker.management.services.WorkerManagerMBean");
		put(WorkerRecoveryManagerImpl.class, null);
		put(ReflectionAdapterImpl.class, null);
        put(SessionDataHandlerImpl.class, "sessionDataHandler");
		put(SynchronizationManagerImpl.class, null);
        put(WorkerConfigurationServiceImpl.class, "workerConfiguration");

        //Monitors
        put(WorkerExecutionMonitorServiceImpl.class, "workerExecutionMonitorService");
        put(WorkerMonitorsImpl.class, "workerMonitorsImpl");
        put(ScheduledWorkerLoadMonitor.class, "scheduledWorkerLoadMonitor");
	}};

	private List<ConfValue> configurationValues = Arrays.asList(
			new ConfValue().NAME("inBufferCapacity").DEFAULT(500),
			new ConfValue().NAME("numberOfExecutionThreads").DEFAULT(20),
			new ConfValue().NAME("maxDeltaBetweenDrains").DEFAULT(100)
	);

	private List<ConfValue> schedulerValues = Arrays.asList(
			new ConfValue().NAME("outBufferInterval").DEFAULT(100L),
			new ConfValue().NAME("keepAliveInterval").DEFAULT(10000L),
			new ConfValue().NAME("configRefreshInterval").DEFAULT(1000L),
            new ConfValue().NAME("statisticsInterval").DEFAULT(1000L),
            new ConfValue().NAME("scheduledWorkerMonitorInterval").DEFAULT(10000L),
            new ConfValue().NAME("workerMonitorRefreshInterval").DEFAULT(300000L)
	);

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		registerWorkerUuid(element.getAttribute("uuid"), element.getAttribute("depends-on"), parserContext);
		registerBeans(parserContext);
		registerSpecialBeans(element, parserContext);
		registerConfiguration(DomUtils.getChildElementByTagName(element, "configuration"), parserContext);
		registerScheduler(DomUtils.getChildElementByTagName(element, "scheduler"), parserContext);
		return createRootBeanDefinition();
	}

	private AbstractBeanDefinition createRootBeanDefinition(){
		return BeanDefinitionBuilder.genericBeanDefinition(Object.class).getBeanDefinition();
	}

	private void registerWorkerUuid(String uuid, String dependsOn, ParserContext parserContext) {
		new BeanRegistrator(parserContext)
				.NAME("workerUuid")
				.CLASS(String.class)
				.addConstructorArgValue(uuid)
				.addDependsOn(StringUtils.hasText(dependsOn)? dependsOn.split(","): null)
				.register();
	}

	private void registerBeans(ParserContext parserContext){
		BeanRegistrator beanRegistrator = new BeanRegistrator(parserContext);
		for (Map.Entry<Class<?>,String> entry : beans.entrySet()) {
			beanRegistrator
					.NAME(entry.getValue())
					.CLASS(entry.getKey())
					.register();
		}
	}

	private static void registerSpecialBeans(Element element, ParserContext parserContext) {
		if(!"false".equalsIgnoreCase(element.getAttribute("register"))) {
			new BeanRegistrator(parserContext).CLASS(WorkerRegistration.class).register();
		}
	}

	private void registerConfiguration(Element configurationElement, ParserContext parserContext) {
		for (ConfValue configurationValue : configurationValues) {
			configurationValue.register(configurationElement, parserContext);
		}
	}

	private void registerScheduler(Element schedulerElement, ParserContext parserContext){
		for (ConfValue value : schedulerValues) {
			value.register(schedulerElement, parserContext);
		}
		new XmlBeanDefinitionReader(parserContext.getRegistry())
				.loadBeanDefinitions("META-INF/spring/score/context/scoreWorkerSchedulerContext.xml");
	}

	@Override
	protected boolean shouldGenerateId() {
		return true;
	}

}
