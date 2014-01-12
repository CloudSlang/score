package com.hp.oo.execution.services;

import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.services.QueueStateIdGeneratorService;
import com.hp.oo.orchestrator.services.configuration.WorkerConfigurationService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Levin
 * Date: 19/12/12
 */
@Component
public class SimpleExecutionRunnableFactory implements FactoryBean<SimpleExecutionRunnable> {

	@Autowired
	private ExecutionService executionService;

	@Autowired
	private OutboundBuffer outBuffer;

    @Autowired
   	private InBuffer inBuffer;

	@Autowired
	private ExecutionMessageConverter converter;

	@Autowired
	private EndExecutionCallback endExecutionCallback;

    @Autowired
    private QueueStateIdGeneratorService queueStateIdGeneratorService;

    @Autowired
    private WorkerConfigurationService workerConfigurationService;

    @Resource
	private String workerUuid;

	@Override
	public SimpleExecutionRunnable getObject() {
		return new SimpleExecutionRunnable(
                executionService,
                outBuffer,
                inBuffer,
                converter,
                endExecutionCallback,
                queueStateIdGeneratorService,
                workerUuid,
                workerConfigurationService
        );
	}

	@Override
	public Class<?> getObjectType() {
		return SimpleExecutionRunnable.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
}
