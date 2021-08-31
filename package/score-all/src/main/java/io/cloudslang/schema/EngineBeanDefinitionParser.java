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

package io.cloudslang.schema;

import io.cloudslang.engine.data.DataBaseDetector;
import io.cloudslang.engine.data.HiloFactoryBean;
import io.cloudslang.engine.data.SqlInQueryReader;
import io.cloudslang.engine.data.SqlUtils;
import io.cloudslang.engine.node.services.WorkerLockServiceImpl;
import io.cloudslang.engine.node.services.WorkerNodeServiceImpl;
import io.cloudslang.engine.node.services.WorkersMBean;
import io.cloudslang.engine.partitions.services.PartitionCallback;
import io.cloudslang.engine.partitions.services.PartitionServiceImpl;
import io.cloudslang.engine.partitions.services.PartitionTemplateImpl;
import io.cloudslang.engine.partitions.services.PartitionUtils;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepositoryImpl;
import io.cloudslang.engine.queue.services.BusyWorkersServiceImpl;
import io.cloudslang.engine.queue.services.ExecutionQueueServiceImpl;
import io.cloudslang.engine.queue.services.LargeMessagesMonitorServiceImpl;
import io.cloudslang.engine.queue.services.QueueDispatcherServiceImpl;
import io.cloudslang.engine.queue.services.QueueListenerImpl;
import io.cloudslang.engine.queue.services.QueueStateIdGeneratorServiceImpl;
import io.cloudslang.engine.queue.services.ScoreEventFactoryImpl;
import io.cloudslang.engine.queue.services.assigner.ExecutionAssignerServiceImpl;
import io.cloudslang.engine.queue.services.cleaner.QueueCleanerServiceImpl;
import io.cloudslang.engine.queue.services.recovery.ExecutionRecoveryServiceImpl;
import io.cloudslang.engine.queue.services.recovery.MessageRecoveryServiceImpl;
import io.cloudslang.engine.queue.services.recovery.WorkerRecoveryServiceImpl;
import io.cloudslang.engine.versioning.services.VersionServiceImpl;
import io.cloudslang.job.ScoreEngineJobsImpl;
import io.cloudslang.orchestrator.services.CancelExecutionServiceImpl;
import io.cloudslang.orchestrator.services.EngineVersionServiceImpl;
import io.cloudslang.orchestrator.services.ExecutionSerializationUtil;
import io.cloudslang.orchestrator.services.ExecutionStateServiceImpl;
import io.cloudslang.orchestrator.services.MergedConfigurationServiceImpl;
import io.cloudslang.orchestrator.services.OrchestratorDispatcherServiceImpl;
import io.cloudslang.orchestrator.services.RunningExecutionPlanServiceImpl;
import io.cloudslang.orchestrator.services.ScoreDeprecatedImpl;
import io.cloudslang.orchestrator.services.ScoreImpl;
import io.cloudslang.orchestrator.services.ScorePauseResumeImpl;
import io.cloudslang.orchestrator.services.ScoreTriggeringImpl;
import io.cloudslang.orchestrator.services.SplitJoinServiceImpl;
import io.cloudslang.orchestrator.services.StubPauseResumeServiceImpl;
import io.cloudslang.orchestrator.services.SuspendedExecutionCleanerServiceImpl;
import io.cloudslang.orchestrator.services.SuspendedExecutionServiceImpl;
import io.cloudslang.orchestrator.services.WorkerDbSupportServiceImpl;
import io.cloudslang.schema.context.ScoreDatabaseContext;
import io.cloudslang.schema.context.ScoreDefaultDatasourceContext;
import io.cloudslang.worker.execution.services.ExternalExecutionServiceImpl;
import io.cloudslang.orchestrator.services.FinishedExecutionStateCleanerServiceImpl;
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
 */
@SuppressWarnings("unused")
public class EngineBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private final static String ENGINE_JOBS_CONTEXT_LOCATION = "META-INF/spring/score/context/scoreEngineSchedulerContext.xml";

	private Map<Class<?>,String> beans = new HashMap<Class<?>,String>(){{
		put(ScorePauseResumeImpl.class, null);
        put(OrchestratorDispatcherServiceImpl.class, "orchestratorDispatcherService");
        put(ExecutionStateServiceImpl.class, "executionStateService");
		put(ExternalExecutionServiceImpl.class, "externalExecutionService");
		put(QueueDispatcherServiceImpl.class, "queueDispatcherService");
		put(ExecutionQueueServiceImpl.class, "executionQueueService");
		put(ExecutionAssignerServiceImpl.class, "executionAssignerService");
		put(PartitionServiceImpl.class, null);
		put(RunningExecutionPlanServiceImpl.class, "runningEP");
		put(VersionServiceImpl.class, null);
		put(CancelExecutionServiceImpl.class, "cancelExecutionService");
		put(ScoreEventFactoryImpl.class, "scoreEventFactory");
		put(QueueListenerImpl.class, "scoreQueueListenenerImpl");
		put(SplitJoinServiceImpl.class, "splitJoinService");
		put(SuspendedExecutionServiceImpl.class, "suspendedExecutionService");
		put(ExecutionRecoveryServiceImpl.class, null);
		put(WorkerRecoveryServiceImpl.class, null);
        put(MessageRecoveryServiceImpl.class, null);
        put(WorkerLockServiceImpl.class, null);
		put(QueueCleanerServiceImpl.class, null);
		put(QueueStateIdGeneratorServiceImpl.class, null);
		put(ScoreTriggeringImpl.class,null);
		put(SuspendedExecutionCleanerServiceImpl.class, null);

		put(PartitionUtils.class, null);
		put(ExecutionMessageConverter.class, null);
		put(ExecutionSerializationUtil.class, null);
		put(SqlUtils.class, null);
		put(SqlInQueryReader.class, null);
		put(DataBaseDetector.class, null);
		put(LargeMessagesMonitorServiceImpl.class, "largeMessagesMonitorService");
		put(ExecutionQueueRepositoryImpl.class, null);
		put(HiloFactoryBean.class, "scoreHiloFactoryBean");
		put(WorkersMBean.class, "io.cloudslang.engine.node.services.WorkersMBean");
        put(WorkerDbSupportServiceImpl.class, null);
        put(ScoreDeprecatedImpl.class, null);
        put(ScoreEngineJobsImpl.class,"scoreEngineJobs");
		put(BusyWorkersServiceImpl.class,"busyWorkersService");
		put(MergedConfigurationServiceImpl.class,"MergedConfigurationService");
		put(FinishedExecutionStateCleanerServiceImpl.class, null);
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
        registerPauseResume(element,parserContext);
		registerWorkerNodeService(element, parserContext);
		registerEngineVersionService(element, parserContext);
    }

    private void registerPauseResume(Element element, ParserContext parserContext){
        String registerPauseResumeService = element.getAttribute("registerPauseResumeService");
        if(!registerPauseResumeService.equals(Boolean.FALSE.toString())){
            new BeanRegistrator(parserContext).CLASS(StubPauseResumeServiceImpl.class).register();
        }
    }

	private void registerWorkerNodeService(Element element, ParserContext parserContext){
		String registerWorkerNodeService = element.getAttribute("registerWorkerNodeService");
		if(!registerWorkerNodeService.equals(Boolean.FALSE.toString())){
			new BeanRegistrator(parserContext).CLASS(WorkerNodeServiceImpl.class).register();
		}
	}

	private void registerEngineVersionService(Element element, ParserContext parserContext){
		String registerEngineVersionService = element.getAttribute("registerEngineVersionService");
		if(!registerEngineVersionService.equals(Boolean.FALSE.toString())){
			new BeanRegistrator(parserContext).CLASS(EngineVersionServiceImpl.class).register();
		}
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
