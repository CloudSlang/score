/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.schema;

import org.eclipse.score.engine.node.services.WorkerLockServiceImpl;
import org.eclipse.score.engine.node.services.WorkerNodeServiceImpl;
import org.eclipse.score.engine.node.services.WorkersMBean;
import org.eclipse.score.engine.queue.entities.ExecutionMessageConverter;
import org.eclipse.score.engine.queue.repositories.ExecutionQueueRepositoryImpl;
import org.eclipse.score.engine.queue.repositories.callbacks.ExecutionStatesCallback;
import org.eclipse.score.engine.queue.services.ExecutionQueueServiceImpl;
import org.eclipse.score.engine.queue.services.QueueDispatcherServiceImpl;
import org.eclipse.score.engine.queue.services.QueueListenerImpl;
import org.eclipse.score.engine.queue.services.QueueStateIdGeneratorServiceImpl;
import org.eclipse.score.engine.queue.services.ScoreEventFactoryImpl;
import org.eclipse.score.engine.queue.services.assigner.ExecutionAssignerServiceImpl;
import org.eclipse.score.engine.queue.services.cleaner.QueueCleanerServiceImpl;
import org.eclipse.score.engine.queue.services.recovery.ExecutionRecoveryServiceImpl;
import org.eclipse.score.engine.queue.services.recovery.MessageRecoveryServiceImpl;
import org.eclipse.score.engine.queue.services.recovery.WorkerRecoveryServiceImpl;
import org.eclipse.score.engine.versioning.services.VersionServiceImpl;
import org.eclipse.score.orchestrator.services.CancelExecutionServiceImpl;
import org.eclipse.score.orchestrator.services.ExecutionSerializationUtil;
import org.eclipse.score.orchestrator.services.OrchestratorDispatcherServiceImpl;
import org.eclipse.score.orchestrator.services.RunningExecutionPlanServiceImpl;
import org.eclipse.score.orchestrator.services.SplitJoinServiceImpl;
import org.eclipse.score.orchestrator.services.StubPauseResumeServiceImpl;
import org.eclipse.score.orchestrator.services.WorkerDbSupportServiceImpl;
import org.eclipse.score.engine.partitions.services.PartitionCallback;
import org.eclipse.score.engine.partitions.services.PartitionServiceImpl;
import org.eclipse.score.engine.partitions.services.PartitionTemplateImpl;
import org.eclipse.score.engine.partitions.services.PartitionUtils;
import org.eclipse.score.orchestrator.services.ScoreDeprecatedImpl;
import org.eclipse.score.orchestrator.services.ScoreImpl;
import org.eclipse.score.orchestrator.services.ScorePauseResumeImpl;
import org.eclipse.score.orchestrator.services.ScoreTriggeringImpl;
import org.eclipse.score.engine.data.DataBaseDetector;
import org.eclipse.score.engine.data.HiloFactoryBean;
import org.eclipse.score.engine.data.SqlInQueryReader;
import org.eclipse.score.engine.data.SqlUtils;
import org.eclipse.score.job.ScoreEngineJobsImpl;
import org.eclipse.score.schema.context.ScoreDatabaseContext;
import org.eclipse.score.schema.context.ScoreDefaultDatasourceContext;
import org.eclipse.score.orchestrator.services.ExecutionStateServiceImpl;
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
 * @author Dima Rassin
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
		put(WorkersMBean.class, "org.eclipse.score.engine.node.services.WorkersMBean");
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
