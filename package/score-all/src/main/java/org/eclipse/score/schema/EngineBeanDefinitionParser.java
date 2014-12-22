/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.schema;

import org.openscore.engine.node.services.WorkerLockServiceImpl;
import org.openscore.engine.node.services.WorkerNodeServiceImpl;
import org.openscore.engine.node.services.WorkersMBean;
import org.openscore.engine.queue.entities.ExecutionMessageConverter;
import org.openscore.engine.queue.repositories.ExecutionQueueRepositoryImpl;
import org.openscore.engine.queue.repositories.callbacks.ExecutionStatesCallback;
import org.openscore.engine.queue.services.ExecutionQueueServiceImpl;
import org.openscore.engine.queue.services.QueueDispatcherServiceImpl;
import org.openscore.engine.queue.services.QueueListenerImpl;
import org.openscore.engine.queue.services.QueueStateIdGeneratorServiceImpl;
import org.openscore.engine.queue.services.ScoreEventFactoryImpl;
import org.openscore.engine.queue.services.assigner.ExecutionAssignerServiceImpl;
import org.openscore.engine.queue.services.cleaner.QueueCleanerServiceImpl;
import org.openscore.engine.queue.services.recovery.ExecutionRecoveryServiceImpl;
import org.openscore.engine.queue.services.recovery.MessageRecoveryServiceImpl;
import org.openscore.engine.queue.services.recovery.WorkerRecoveryServiceImpl;
import org.openscore.engine.versioning.services.VersionServiceImpl;
import org.openscore.orchestrator.services.CancelExecutionServiceImpl;
import org.openscore.orchestrator.services.ExecutionSerializationUtil;
import org.openscore.orchestrator.services.OrchestratorDispatcherServiceImpl;
import org.openscore.orchestrator.services.RunningExecutionPlanServiceImpl;
import org.openscore.orchestrator.services.SplitJoinServiceImpl;
import org.openscore.orchestrator.services.StubPauseResumeServiceImpl;
import org.openscore.orchestrator.services.WorkerDbSupportServiceImpl;
import org.openscore.engine.partitions.services.PartitionCallback;
import org.openscore.engine.partitions.services.PartitionServiceImpl;
import org.openscore.engine.partitions.services.PartitionTemplateImpl;
import org.openscore.engine.partitions.services.PartitionUtils;
import org.openscore.orchestrator.services.ScoreDeprecatedImpl;
import org.openscore.orchestrator.services.ScoreImpl;
import org.openscore.orchestrator.services.ScorePauseResumeImpl;
import org.openscore.orchestrator.services.ScoreTriggeringImpl;
import org.openscore.engine.data.DataBaseDetector;
import org.openscore.engine.data.HiloFactoryBean;
import org.openscore.engine.data.SqlInQueryReader;
import org.openscore.engine.data.SqlUtils;
import org.openscore.job.ScoreEngineJobsImpl;
import org.eclipse.score.schema.context.ScoreDatabaseContext;
import org.eclipse.score.schema.context.ScoreDefaultDatasourceContext;
import org.openscore.orchestrator.services.ExecutionStateServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 1/21/14
 *
 * @author
 */
@SuppressWarnings("unused")
public class EngineBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private final static String ENGINE_JOBS_CONTEXT_LOCATION = "META-INF/spring/score/context/scoreEngineSchedulerContext.xml";

	private Map<Class<?>,String> beans = new HashMap<Class<?>,String>(){{
		put(ScorePauseResumeImpl.class, null);
        put(OrchestratorDispatcherServiceImpl.class, "orchestratorDispatcherService");
        put(ExecutionStateServiceImpl.class, null);
		put(QueueDispatcherServiceImpl.class, "queueDispatcherService");
		put(ExecutionQueueServiceImpl.class, "executionQueueService");
		put(ExecutionAssignerServiceImpl.class, "executionAssignerService");
		put(PartitionServiceImpl.class, null);
		put(RunningExecutionPlanServiceImpl.class, "runningEP");
		put(WorkerNodeServiceImpl.class, null);
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
		put(WorkersMBean.class, "WorkersMBean");
		put(ExecutionStatesCallback.class, "executionStatesCallback");
        put(WorkerDbSupportServiceImpl.class, null);
        put(ScoreDeprecatedImpl.class, null);
        put(ScoreEngineJobsImpl.class,"scoreEngineJobs");
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
        if(StringUtils.isNotBlank(ignoreEngineJobs) && ignoreEngineJobs.equals(Boolean.TRUE.toString())){
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
        registerPartitionTemplates(parserContext);
        registerPauseResume(element,parserContext);
    }

    private void registerPauseResume(Element element, ParserContext parserContext){
        String registerPauseResumeService = element.getAttribute("registerPauseResumeService");
        if(!registerPauseResumeService.equals(Boolean.FALSE.toString())){
            new BeanRegistrator(parserContext).CLASS(StubPauseResumeServiceImpl.class).register();
        }
    }

	private void registerPartitionTemplates(ParserContext parserContext) {
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
