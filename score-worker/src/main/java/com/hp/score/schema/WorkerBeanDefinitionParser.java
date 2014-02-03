package com.hp.score.schema;

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
 * Date: 1/21/14
 *
 * @author Dima Rassin
 */
@SuppressWarnings("unused")
public class WorkerBeanDefinitionParser extends AbstractBeanDefinitionParser {

	private Map<Class<?>,String> beans = new HashMap<Class<?>,String>(){{
		put(com.hp.oo.execution.services.WorkerManager.class, "workerManager");
		put(com.hp.oo.execution.services.EventBusImpl.class, null);
		put(com.hp.oo.execution.services.ExecutionServiceImpl.class, "agent");
		put(com.hp.oo.execution.services.InBuffer.class, null);
		put(com.hp.oo.execution.services.OutboundBufferImpl.class, "outBuffer");
		put(com.hp.oo.execution.services.RetryTemplate.class, null);
		put(com.hp.oo.execution.services.SimpleExecutionRunnableFactory.class, null);
		put(com.hp.oo.execution.services.WorkerManagerMBean.class, null);
		put(com.hp.oo.execution.services.WorkerRecoveryManagerImpl.class, null);
		put(com.hp.oo.execution.gateways.ExecutionGatewayImpl.class, "runningExecutionGateway");
		put(com.hp.oo.execution.reflection.ReflectionAdapterImpl.class, null);
	}};

	private List<ConfValue> configurationValues = Arrays.asList(
			new ConfValue().NAME("inBufferCapacity").DEFAULT(500),
			new ConfValue().NAME("numberOfExecutionThreads").DEFAULT(20),
			new ConfValue().NAME("maxDeltaBetweenDrains").DEFAULT(100)
	);

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		registerWorkerUuid(element.getAttribute("uuid"), element.getAttribute("depends-on"), parserContext);

		registerBeans(parserContext);

		registerConfiguration(DomUtils.getChildElementByTagName(element, "configuration"), parserContext);

		registerSpringIntegration(parserContext);

		return createRootBeanDefinition();
	}

	private AbstractBeanDefinition createRootBeanDefinition(){
		//
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

	private void registerConfiguration(Element configurationElement, ParserContext parserContext) {
		Map<String,Object> configuration = parseConfiguration(configurationElement);

		BeanRegistrator beanRegistrator = new BeanRegistrator(parserContext);
		for (Map.Entry<String, Object> configurationEntry : configuration.entrySet()) {
			beanRegistrator
					.NAME(configurationEntry.getKey())
					.CLASS(configurationEntry.getValue().getClass())
					.addConstructorArgValue(configurationEntry.getValue())
					.register();
		}
	}
	
	private Map<String,Object> parseConfiguration(Element configurationElement){
		Map<String,Object> configuration = new HashMap<>();
		for (ConfValue value : configurationValues) {
			String attrValue = configurationElement != null? configurationElement.getAttribute(value.NAME()): null;
			configuration.put(value.NAME(), value.fromString(attrValue));
		}
		return configuration;
	}

	private void registerSpringIntegration(ParserContext parserContext) {
		new XmlBeanDefinitionReader(parserContext.getRegistry())
				.loadBeanDefinitions("META-INF/spring/score/context/scoreIntegrationContext.xml");
	}

	@Override
	protected boolean shouldGenerateId() {
		return true;
	}

	private class ConfValue {
		private String name;
		private Class<?> clazz;
		private Object defaultValue;

		public String NAME(){
			return this.name;
		}

		public ConfValue NAME(String name) {
			this.name = name;
			return this;
		}

		public ConfValue CLASS(Class<?> clazz) {
			this.clazz = clazz;
			return this;
		}

		public ConfValue DEFAULT(Object defaultValue) {
			this.defaultValue = defaultValue;
			if (defaultValue != null) this.clazz = defaultValue.getClass();
			return this;
		}

		public Object fromString(String value) {
			if (StringUtils.hasText(value)){
				try {
					return clazz.getConstructor(String.class).newInstance(value);
				} catch (Exception ex) {
					throw new RuntimeException("Failed to parse worker configuration attribute [" + name + "] value: " + value, ex);
				}
			} else {
				return defaultValue;
			}
		}
	}
}