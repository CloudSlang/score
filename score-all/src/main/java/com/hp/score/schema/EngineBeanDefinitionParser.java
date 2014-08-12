package com.hp.score.schema;

import com.hp.oo.engine.node.services.WorkerLockServiceImpl;
import com.hp.oo.engine.node.services.WorkerNodeServiceImpl;
import com.hp.oo.engine.node.services.WorkersMBean;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.repositories.ExecutionQueueRepositoryImpl;
import com.hp.oo.engine.queue.repositories.callbacks.ExecutionStatesCallback;
import com.hp.oo.engine.queue.services.ExecutionQueueServiceImpl;
import com.hp.oo.engine.queue.services.QueueDispatcherServiceImpl;
import com.hp.oo.engine.queue.services.QueueListenerImpl;
import com.hp.oo.engine.queue.services.QueueStateIdGeneratorServiceImpl;
import com.hp.oo.engine.queue.services.ScoreEventFactoryImpl;
import com.hp.oo.engine.queue.services.assigner.ExecutionAssignerServiceImpl;
import com.hp.oo.engine.queue.services.cleaner.QueueCleanerServiceImpl;
import com.hp.oo.engine.queue.services.recovery.ExecutionRecoveryServiceImpl;
import com.hp.oo.engine.queue.services.recovery.MessageRecoveryServiceImpl;
import com.hp.oo.engine.queue.services.recovery.WorkerRecoveryServiceImpl;
import com.hp.oo.engine.versioning.services.VersionServiceImpl;
import com.hp.oo.orchestrator.services.CancelExecutionServiceImpl;
import com.hp.oo.orchestrator.services.ExecutionSerializationUtil;
import com.hp.oo.orchestrator.services.OrchestratorDispatcherServiceImpl;
import com.hp.oo.orchestrator.services.OrchestratorServiceImpl;
import com.hp.oo.orchestrator.services.RunningExecutionPlanServiceImpl;
import com.hp.oo.orchestrator.services.SplitJoinServiceImpl;
import com.hp.oo.orchestrator.services.WorkerDbSupportServiceImpl;
import com.hp.oo.partitions.services.PartitionCallback;
import com.hp.oo.partitions.services.PartitionServiceImpl;
import com.hp.oo.partitions.services.PartitionTemplateImpl;
import com.hp.oo.partitions.services.PartitionUtils;
import com.hp.score.ScoreDeprecatedImpl;
import com.hp.score.ScoreImpl;
import com.hp.score.ScorePauseResumeImpl;
import com.hp.score.ScoreTriggeringImpl;
import com.hp.score.engine.data.DataBaseDetector;
import com.hp.score.engine.data.HiloFactoryBean;
import com.hp.score.engine.data.SqlInQueryReader;
import com.hp.score.engine.data.SqlUtils;
import com.hp.score.schema.context.ScoreDatabaseContext;
import com.hp.score.schema.context.ScoreDefaultDatasourceContext;
import com.hp.score.services.ExecutionStateServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
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

    private final static String ENGINE_JOBS_CONTEXT_LOCATION = "META-INF/spring/score/context/scoreEngineSchedulerContext.xml";

	private Map<Class<?>,String> beans = new HashMap<Class<?>,String>(){{
		put(ScorePauseResumeImpl.class, null);
		put(OrchestratorServiceImpl.class, "orchestratorService");
        put(OrchestratorDispatcherServiceImpl.class, "orchestratorDispatcherService");
        put(ExecutionStateServiceImpl.class, null);
		put(QueueDispatcherServiceImpl.class, "queueDispatcherService");
		put(ExecutionQueueServiceImpl.class, "executionQueueService");
		put(ExecutionAssignerServiceImpl.class, "executionAssignerService");
		put(PartitionServiceImpl.class, null);
		put(RunningExecutionPlanServiceImpl.class, "runningEP");
		put(WorkerNodeServiceImpl.class, "ooUserDetailsService");
		put(VersionServiceImpl.class, null);
		put(CancelExecutionServiceImpl.class, "cancelExecutionService");
		put(ScoreEventFactoryImpl.class, "scoreEventFactory");
		put(QueueListenerImpl.class, "scoreQueueListenenerImpl");
		put(SplitJoinServiceImpl.class, "splitJoinService");
		put(ExecutionRecoveryServiceImpl.class, null);
		put(WorkerRecoveryServiceImpl.class, null);
        put(MessageRecoveryServiceImpl.class, null);
        put(WorkerLockServiceImpl.class, null);
		put(QueueCleanerServiceImpl.class, null);
		put(QueueStateIdGeneratorServiceImpl.class, null);
        put(ScoreTriggeringImpl.class,null);

		put(PartitionUtils.class, null);
		put(ExecutionMessageConverter.class, null);
		put(ExecutionSerializationUtil.class, null);
		put(SqlUtils.class, null);
		put(SqlInQueryReader.class, null);
		put(DataBaseDetector.class, null);
		put(ExecutionQueueRepositoryImpl.class, null);
		put(HiloFactoryBean.class, "scoreHiloFactoryBean");
		put(WorkersMBean.class, "com.hp.oo.engine.node.services.WorkersMBean");
		put(ExecutionStatesCallback.class, "executionStatesCallback");
        put(WorkerDbSupportServiceImpl.class, null);
        put(ScoreDeprecatedImpl.class, null);
	}};

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		registerBeans(parserContext);

        loadContexts(element, parserContext.getRegistry());

		registerSpecialBeans(element, parserContext);

		return BeanDefinitionBuilder.genericBeanDefinition(ScoreImpl.class).getBeanDefinition();
	}

    private void loadContexts(Element element, BeanDefinitionRegistry beanDefinitionRegistry) {
        String externalDatabase = element.getAttribute("externalDatabase");
        if (StringUtils.isBlank(externalDatabase) || externalDatabase.equals(Boolean.FALSE.toString())) {
            AnnotatedBeanDefinitionReader definitionReader = new AnnotatedBeanDefinitionReader(beanDefinitionRegistry);
            definitionReader.register(ScoreDefaultDatasourceContext.class);
            definitionReader.register(ScoreDatabaseContext.class);
        }

        String repositoriesContextPath = "META-INF/spring/score/context/scoreRepositoryContext.xml";

        String ignoreEngineJobs = element.getAttribute("ignoreEngineJobs");
        if(!StringUtils.isBlank(ignoreEngineJobs) && ignoreEngineJobs.equals(Boolean.TRUE.toString())){
            new XmlBeanDefinitionReader(beanDefinitionRegistry).loadBeanDefinitions(repositoriesContextPath);
        }
        else{
            new XmlBeanDefinitionReader(beanDefinitionRegistry).loadBeanDefinitions(repositoriesContextPath,ENGINE_JOBS_CONTEXT_LOCATION);
        }
    }

    private void registerBeans(ParserContext parserContext) {
        BeanRegistrator beanRegistrator = new BeanRegistrator(parserContext);
        for (Map.Entry<Class<?>, String> entry : beans.entrySet()) {
            beanRegistrator
					.NAME(entry.getValue())
					.CLASS(entry.getKey())
					.register();
		}
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
		//registerPartitionTemplate("OO_EXECUTION_EVENTS", 4, 1000000, -1, parserContext,ExecutionEventsCallback);
		registerPartitionTemplate("OO_EXECUTION_STATES", 2, 50000, -1, parserContext,ExecutionStatesCallback.class);
	}

	private void registerPartitionTemplate(String name, int groupSize, long sizeThreshold, long timeThreshold,
                                           ParserContext parserContext,
                                           Class<? extends PartitionCallback> callbackClass){
		new BeanRegistrator(parserContext)
				.NAME(name)
				.CLASS(PartitionTemplateImpl.class)
				.addPropertyValue("groupSize", groupSize)
				.addPropertyValue("sizeThreshold", sizeThreshold)
				.addPropertyValue("timeThreshold", timeThreshold)
                .addPropertyValue("callbackClass",callbackClass)
				.register();
	}

	@Override
	protected boolean shouldGenerateId() {
		return true;
	}
}
