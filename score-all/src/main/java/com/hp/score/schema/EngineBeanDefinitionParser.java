package com.hp.score.schema;

import com.hp.oo.engine.node.services.WorkerNodeServiceImpl;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.repositories.ExecutionQueueRepositoryImpl;
import com.hp.oo.engine.queue.services.ExecutionQueueServiceImpl;
import com.hp.oo.engine.queue.services.QueueDispatcherServiceImpl;
import com.hp.oo.engine.queue.services.assigner.ExecutionAssignerServiceImpl;
import com.hp.oo.engine.versioning.services.VersionServiceImpl;
import com.hp.oo.orchestrator.repositories.ExecutionSummaryExpressions;
import com.hp.oo.orchestrator.services.ExecConfigSerializationUtil;
import com.hp.oo.orchestrator.services.ExecutionSerializationUtil;
import com.hp.oo.orchestrator.services.ExecutionSummaryServiceImpl;
import com.hp.oo.orchestrator.services.RunningExecutionConfigurationServiceImpl;
import com.hp.oo.orchestrator.services.RunningExecutionPlanServiceImpl;
import com.hp.oo.partitions.services.PartitionServiceImpl;
import com.hp.oo.partitions.services.PartitionTemplateImpl;
import com.hp.oo.partitions.services.PartitionUtils;
import com.hp.score.ScoreImpl;
import com.hp.score.engine.data.DataBaseDetector;
import com.hp.score.engine.data.HiloFactoryBean;
import com.hp.score.engine.data.SqlUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.w3c.dom.Element;

/**
 * Date: 1/21/14
 *
 * @author Dima Rassin
 */
@SuppressWarnings("unused")
public class EngineBeanDefinitionParser extends AbstractBeanDefinitionParser {
	private final Logger logger = Logger.getLogger(getClass());

	private Class[] beans = new Class[]{
			ExecutionSummaryServiceImpl.class,
			QueueDispatcherServiceImpl.class,
			ExecutionQueueServiceImpl.class,
			RunningExecutionConfigurationServiceImpl.class,
			ExecutionAssignerServiceImpl.class,
			PartitionServiceImpl.class,
			RunningExecutionPlanServiceImpl.class,
			PartitionUtils.class,
			WorkerNodeServiceImpl.class,
			VersionServiceImpl.class,
			ExecutionMessageConverter.class,
			ExecutionSerializationUtil.class,
			ExecConfigSerializationUtil.class,
			ExecutionSummaryExpressions.class,
			SqlUtils.class,
			DataBaseDetector.class,
			ExecutionQueueRepositoryImpl.class,
			HiloFactoryBean.class,
	};

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		registerBeans(parserContext);

		registerRepositoryBeans(parserContext);

		registerSpecialBeans(element, parserContext);

		return BeanDefinitionBuilder.rootBeanDefinition(ScoreImpl.class).getBeanDefinition();
	}

	private void registerBeans(ParserContext parserContext){
		// register beans
		for (Class beanClass : beans) {
			registerBean(parserContext, beanClass);
		}
	}

	private void registerRepositoryBeans(ParserContext parserContext){
		new XmlBeanDefinitionReader(parserContext.getRegistry())
				.loadBeanDefinitions("META-INF/spring/score/context/scoreRepositoryContext.xml");
	}

	private void registerSpecialBeans(Element element, ParserContext parserContext) {
		registerMessageDigestPasswordEncoder(element.getAttribute("messageDigestAlgorithm"), parserContext);
		registerPartitionTemplates(parserContext);
		registerJdbcTemplate(parserContext);
	}

	private void registerMessageDigestPasswordEncoder(String algorithm, ParserContext parserContext) {
		if (algorithm == null || algorithm.isEmpty()) algorithm = "sha-256";
		registerBean(parserContext, BeanDefinitionBuilder
				.genericBeanDefinition(MessageDigestPasswordEncoder.class)
				.addConstructorArgValue(algorithm)
		);
	}

	private void registerPartitionTemplates(ParserContext parserContext) {
		registerPartitionTemplate("OO_EXECUTION_EVENTS", 4, 1000000, -1, parserContext);
		registerPartitionTemplate("OO_EXECUTION_STATES", 2, 50000, -1, parserContext);
	}

	private void registerPartitionTemplate(String name, int groupSize, long sizeThreshold, long timeThreshold, ParserContext parserContext){
		parserContext.getRegistry().registerBeanDefinition(name, BeanDefinitionBuilder
				.genericBeanDefinition(PartitionTemplateImpl.class)
				.addPropertyValue("groupSize", groupSize)
				.addPropertyValue("sizeThreshold", sizeThreshold)
				.addPropertyValue("timeThreshold", timeThreshold)
				.getBeanDefinition());
	}

	private void registerJdbcTemplate(ParserContext parserContext) {
		registerBean(parserContext, BeanDefinitionBuilder
				.genericBeanDefinition(JdbcTemplate.class)
				.addConstructorArgReference("dataSource")
		);
	}



	// ---------------------------------------------------------------------------------------------
	private void registerBean(ParserContext parserContext, Class<?> beanClass){
		registerBean(parserContext, BeanDefinitionBuilder.genericBeanDefinition(beanClass));
	}

	private void registerBean(ParserContext parserContext, BeanDefinitionBuilder beanDefinitionBuilder){
		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
		parserContext.getRegistry().registerBeanDefinition(resolveBeanName(beanDefinition.getBeanClass()), beanDefinition);
	}

	private String resolveBeanName(Class<?> beanClass){
		return beanClass.getSimpleName();
	}
}
