package com.hp.score.schema;

import com.hp.oo.engine.node.services.WorkerNodeServiceImpl;
import com.hp.oo.engine.node.services.WorkersMBean;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.repositories.ExecutionQueueRepositoryImpl;
import com.hp.oo.engine.queue.repositories.callbacks.ExecutionStatesCallback;
import com.hp.oo.engine.queue.services.ExecutionQueueServiceImpl;
import com.hp.oo.engine.queue.services.QueueDispatcherServiceImpl;
import com.hp.oo.engine.queue.services.QueueStateIdGeneratorServiceImpl;
import com.hp.oo.engine.queue.services.assigner.ExecutionAssignerServiceImpl;
import com.hp.oo.engine.queue.services.cleaner.QueueCleanerServiceImpl;
import com.hp.oo.engine.queue.services.recovery.ExecutionRecoveryServiceImpl;
import com.hp.oo.engine.versioning.services.VersionServiceImpl;
import com.hp.oo.orchestrator.repositories.ExecutionSummaryExpressions;
import com.hp.oo.orchestrator.services.CancelExecutionServiceImpl;
import com.hp.oo.orchestrator.services.ExecConfigSerializationUtil;
import com.hp.oo.orchestrator.services.ExecutionBoundInputsServiceImpl;
import com.hp.oo.orchestrator.services.ExecutionInterruptsSerializationUtil;
import com.hp.oo.orchestrator.services.ExecutionInterruptsServiceImpl;
import com.hp.oo.orchestrator.services.ExecutionSerializationUtil;
import com.hp.oo.orchestrator.services.ExecutionSummaryServiceImpl;
import com.hp.oo.orchestrator.services.OrchestratorDispatcherServiceImpl;
import com.hp.oo.orchestrator.services.OrchestratorServiceImpl;
import com.hp.oo.orchestrator.services.RunningExecutionConfigurationServiceImpl;
import com.hp.oo.orchestrator.services.RunningExecutionPlanServiceImpl;
import com.hp.oo.orchestrator.services.SplitJoinServiceImpl;
import com.hp.oo.partitions.services.PartitionServiceImpl;
import com.hp.oo.partitions.services.PartitionTemplateImpl;
import com.hp.oo.partitions.services.PartitionUtils;
import com.hp.score.ScoreImpl;
import com.hp.score.engine.data.DataBaseDetector;
import com.hp.score.engine.data.HiloFactoryBean;
import com.hp.score.engine.data.SqlInQueryReader;
import com.hp.score.engine.data.SqlUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 1/21/14
 *
 * @author Dima Rassin
 */
@SuppressWarnings("unused")
public class EngineBeanDefinitionParser extends AbstractBeanDefinitionParser {
	private final Logger logger = Logger.getLogger(getClass());

	private Map<Class<?>,String> beans = new HashMap<Class<?>,String>(){{
		put(OrchestratorServiceImpl.class, "orchestratorService");
		put(OrchestratorDispatcherServiceImpl.class, "orchestratorDispatcherService");
		put(ExecutionSummaryServiceImpl.class, null);
		put(QueueDispatcherServiceImpl.class, "queueDispatcherService");
		put(ExecutionQueueServiceImpl.class, "executionQueueService");
		put(RunningExecutionConfigurationServiceImpl.class, "runningExecutionConfigurationService");
		put(ExecutionAssignerServiceImpl.class, "executionAssignerService");
		put(PartitionServiceImpl.class, null);
		put(RunningExecutionPlanServiceImpl.class, "runningEP");
		put(WorkerNodeServiceImpl.class, "ooUserDetailsService");
		put(VersionServiceImpl.class, null);
		put(CancelExecutionServiceImpl.class, "cancelExecutionService");
		put(ExecutionBoundInputsServiceImpl.class, null);
		put(ExecutionInterruptsServiceImpl.class, "executionInterruptsService");
		put(SplitJoinServiceImpl.class, "splitJoinService");
		put(ExecutionRecoveryServiceImpl.class, null);
		put(QueueCleanerServiceImpl.class, null);
		put(QueueStateIdGeneratorServiceImpl.class, null);

		put(PartitionUtils.class, null);
		put(ExecutionMessageConverter.class, null);
		put(ExecutionSerializationUtil.class, null);
		put(ExecConfigSerializationUtil.class, null);
		put(ExecutionSummaryExpressions.class, null);
		put(SqlUtils.class, null);
		put(SqlInQueryReader.class, null);
		put(DataBaseDetector.class, null);
		put(ExecutionQueueRepositoryImpl.class, null);
		put(HiloFactoryBean.class, "scoreHiloFactoryBean");
		put(WorkersMBean.class, null);
		put(ExecutionInterruptsSerializationUtil.class, null);
		put(ExecutionStatesCallback.class, "executionStatesCallback");
	}};

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		registerBeans(parserContext);

		registerRepositoryBeans(parserContext);

		registerSpecialBeans(element, parserContext);

		return BeanDefinitionBuilder.genericBeanDefinition(ScoreImpl.class).getBeanDefinition();
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

	private void registerRepositoryBeans(ParserContext parserContext){
		new XmlBeanDefinitionReader(parserContext.getRegistry())
				.loadBeanDefinitions("META-INF/spring/score/context/scoreRepositoryContext.xml");
	}

	private void registerSpecialBeans(Element element, ParserContext parserContext) {
		registerMessageDigestPasswordEncoder(element.getAttribute("messageDigestAlgorithm"), parserContext);
		registerPartitionTemplates(parserContext);
	}

	private void registerMessageDigestPasswordEncoder(String algorithm, ParserContext parserContext) {
		if (algorithm == null || algorithm.isEmpty()) algorithm = "sha-256";

		new BeanRegistrator(parserContext)
				.CLASS(MessageDigestPasswordEncoder.class)
				.addConstructorArgValue(algorithm)
				.register();
	}

	private void registerPartitionTemplates(ParserContext parserContext) {
		registerPartitionTemplate("OO_EXECUTION_EVENTS", 4, 1000000, -1, parserContext);
		registerPartitionTemplate("OO_EXECUTION_STATES", 2, 50000, -1, parserContext);
	}

	private void registerPartitionTemplate(String name, int groupSize, long sizeThreshold, long timeThreshold, ParserContext parserContext){
		new BeanRegistrator(parserContext)
				.NAME(name)
				.CLASS(PartitionTemplateImpl.class)
				.addPropertyValue("groupSize", groupSize)
				.addPropertyValue("sizeThreshold", sizeThreshold)
				.addPropertyValue("timeThreshold", timeThreshold)
				.register();
	}

	@Override
	protected boolean shouldGenerateId() {
		return true;
	}
}
