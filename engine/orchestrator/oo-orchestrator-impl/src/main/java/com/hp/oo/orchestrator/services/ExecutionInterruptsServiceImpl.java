package com.hp.oo.orchestrator.services;

import com.hp.oo.execution.debug.Breakpoint;
import com.hp.oo.execution.debug.ExecutionInterrupt;
import com.hp.oo.execution.debug.ResponseOverride;
import com.hp.oo.execution.services.ExecutionInterruptsService;
import com.hp.oo.orchestrator.entities.ExecutionInterrupts;
import com.hp.oo.orchestrator.entities.debug.AbstractExecutionInterruptRegistry;
import com.hp.oo.orchestrator.entities.debug.ResponseOverrideRegistry;
import com.hp.oo.orchestrator.repositories.ExecutionInterruptsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hp.oo.execution.debug.ExecutionInterrupt.InterruptType;

/**
 * Created with IntelliJ IDEA.
 * User: hajyhia
 * Date: 2/24/13
 * Time: 5:02 PM
 */

@Service("executionInterruptsService")
public final class ExecutionInterruptsServiceImpl implements ExecutionInterruptsService {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    ExecutionInterruptsRepository repository;

    @Autowired
    ExecutionInterruptsSerializationUtil serializationUtil;

    @Override
    @Transactional
    public Long createExecutionBreakpointInterrupts(String executionId, Set<ExecutionInterrupt> interrupts) {

        if(StringUtils.isEmpty(executionId)){
            throw new RuntimeException("executionId is null");
        }
        if(interrupts.isEmpty() )  return null;
        ExecutionInterrupts executionInterrupts = repository.findByExecutionIdAndType(executionId, InterruptType.BREAKPOINT.name());
        if(executionInterrupts == null){
            executionInterrupts = createExecutionDebugInterrupts(executionId, InterruptType.BREAKPOINT);
        }

        AbstractExecutionInterruptRegistry breakpointRegistry = serializationUtil.objFromBytes(executionInterrupts.getExecutionInterruptRegistry());
        if(breakpointRegistry == null){
            breakpointRegistry = new ResponseOverrideRegistry();
        }

        registerDebugInterrupt(interrupts, breakpointRegistry, true);

        executionInterrupts.setExecutionInterruptRegistry(serializationUtil.objToBytes(breakpointRegistry));

        ExecutionInterrupts saved = repository.save(executionInterrupts);

        return saved.getId();
    }



    @Override
    @Transactional
    public Long createExecutionResponsesOverrideInterrupts(String executionId, Set<ExecutionInterrupt> interrupts) {
        if(StringUtils.isEmpty(executionId)){
            throw new RuntimeException("executionId is null");
        }
        if(interrupts.isEmpty() )  return null;
        ExecutionInterrupts executionInterrupts = repository.findByExecutionIdAndType(executionId, InterruptType.OVERRIDE_RESPONSES.name());
        if(executionInterrupts == null){
            executionInterrupts = createExecutionDebugInterrupts(executionId, InterruptType.OVERRIDE_RESPONSES);
        }

        AbstractExecutionInterruptRegistry breakpointRegistry = serializationUtil.objFromBytes(executionInterrupts.getExecutionInterruptRegistry());
        if(breakpointRegistry == null){
            breakpointRegistry = new ResponseOverrideRegistry();
        }


        ExecutionInterrupt interrupt = interrupts.iterator().next();
        if(ResponseOverride.UNIVERSAL_OVERRIDE_RESPONSES.getUUID().equals(interrupt.getUUID())){
            breakpointRegistry.setInterruptAll(interrupt.isEnabled());
        }else {
            registerDebugInterrupt(interrupts, breakpointRegistry, true);
        }

        executionInterrupts.setExecutionInterruptRegistry(serializationUtil.objToBytes(breakpointRegistry));

        ExecutionInterrupts saved = repository.save(executionInterrupts);

        return saved.getId();
    }

    /**
     *
     * @param executionId
     * @param interruptType
     * @param key
     * @param value
     * @return
     */
    @Override
    @Transactional
    public ExecutionInterrupt readExecutionDebugInterrupts(String executionId, InterruptType interruptType, String key, String value) {

        return readExecutionDebugInterrupts(executionId,interruptType,key,value,false);
    }

    /**
     * invoke when you desire to retrieve ExecutionInterrupt and an un-register it immediately
     * @param executionId
     * @param interruptType  :BREAKPOINT,OVERRIDE_RESPONSES
     * @param key : used to match value
     * @param value :key value
     * @return
     */
    @Override
    @Transactional
    public ExecutionInterrupt readExecutionDebugInterrupts(String executionId, InterruptType interruptType, String key, String value, boolean unRegister) {

        if(StringUtils.isEmpty(executionId)){
            throw new RuntimeException("executionId null");
        }

        ExecutionInterrupts executionDebugInterrupts = repository.findByExecutionIdAndType(executionId, interruptType.name());
        if(executionDebugInterrupts == null){
            return null;
        }
        AbstractExecutionInterruptRegistry executionInterruptRegistry = serializationUtil.objFromBytes(executionDebugInterrupts.getExecutionInterruptRegistry());

        if(executionInterruptRegistry == null){
            return null;
        }

        if(executionInterruptRegistry.isInterruptAll()){
            return executionInterruptRegistry.getUniversalInterrupt();
        }

        ExecutionInterrupt executionInterrupt = executionInterruptRegistry.getInterruptByKey(key,value);
        if(executionInterrupt !=null && unRegister){
            executionInterruptRegistry.unregisterDebugInterrupt(executionInterrupt);

            if(executionInterruptRegistry.size()==0){
                logger.debug("Remove " + interruptType + " interrupts registry of ExecutionId: " + executionId + ", registered interrupts size is zero");
                removeExecutionDebugInterrupts(executionId, interruptType);
            }else {
                executionDebugInterrupts.setExecutionInterruptRegistry(serializationUtil.objToBytes(executionInterruptRegistry));
                repository.save(executionDebugInterrupts);
            }
        }
        return executionInterrupt;
    }

    @Override
    @Transactional
    public Long setExecutionOverrideAllResponses(String executionId, boolean overrideAll) {
        if(StringUtils.isEmpty(executionId)){
            logger.error("executionId null");
            throw new RuntimeException("executionId null");
        }

        ExecutionInterrupts executionInterrupts = repository.findByExecutionIdAndType(executionId, InterruptType.OVERRIDE_RESPONSES.name());
        if(executionInterrupts == null){
            executionInterrupts = createExecutionDebugInterrupts(executionId, InterruptType.OVERRIDE_RESPONSES);
        }
        AbstractExecutionInterruptRegistry breakpointRegistry = serializationUtil.objFromBytes(executionInterrupts.getExecutionInterruptRegistry());
        if(breakpointRegistry == null){
            breakpointRegistry = new ResponseOverrideRegistry();
        }

        breakpointRegistry.setInterruptAll(overrideAll);

        executionInterrupts.setExecutionInterruptRegistry(serializationUtil.objToBytes(breakpointRegistry));

        ExecutionInterrupts saved = repository.save(executionInterrupts);
        return saved.getId();
    }

    @Override
    @Transactional
    public void removeExecutionDebugInterrupts(String executionId) {

        if(StringUtils.isEmpty(executionId)){
            logger.error("executionId null");
            throw new RuntimeException("executionId null");
        }
        List<ExecutionInterrupts> all = repository.findByExecutionId(executionId);
        if(all != null && !all.isEmpty()){
            repository.delete(all);
        }
    }



    @Override
    @Transactional
    public void removeExecutionDebugInterrupts(String executionId, InterruptType interruptType) {

        if(StringUtils.isEmpty(executionId)){
            logger.error("executionId null");
            throw new RuntimeException("executionId null");
        }
        ExecutionInterrupts all = repository.findByExecutionIdAndType(executionId,interruptType.name());
        if(all != null){
            repository.delete(all);
        }
    }



    @Override
    @Transactional
    public Long createExecutionDebugInterrupts(String executionId, Map<String, String> map) {
        try {
            String breakpointsAsString = map.get(InterruptType.BREAKPOINT.name());
            if(!StringUtils.isEmpty(breakpointsAsString)){

                if (logger.isDebugEnabled()) {
                    logger.debug("Create Debug Interrupts of type " + InterruptType.BREAKPOINT.name() + "for execution id: " + executionId );
                }
                Set<ExecutionInterrupt> breakpoints = jsonMapper.readValue(breakpointsAsString,new TypeReference<Set<Breakpoint>>() {});


                if(breakpoints.isEmpty() ){
                    removeExecutionDebugInterrupts(executionId,InterruptType.BREAKPOINT);
                    return null;
                }  else {
                    return createExecutionBreakpointInterrupts(executionId,breakpoints);
                }
            }

            String overrideResponsesAsString = map.get(InterruptType.OVERRIDE_RESPONSES.name());
            if(!StringUtils.isEmpty(overrideResponsesAsString)){

                if (logger.isDebugEnabled()) {
                    logger.debug("Create Debug Interrupts of type " + InterruptType.OVERRIDE_RESPONSES.name() + "for execution id: " + executionId );
                }
                Set<ExecutionInterrupt> overrideResponses = jsonMapper.readValue(overrideResponsesAsString,new TypeReference<Set<ResponseOverride>>() {});

                if(overrideResponses.isEmpty() ){
                    removeExecutionDebugInterrupts(executionId,InterruptType.OVERRIDE_RESPONSES);
                    return null;
                }else {
                    return createExecutionResponsesOverrideInterrupts(executionId,overrideResponses);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private void registerDebugInterrupt(Set<ExecutionInterrupt> executionInterrupts, AbstractExecutionInterruptRegistry breakpointRegistry, boolean clean) {

        breakpointRegistry.clearAll();

        if(executionInterrupts!=null){
            for (ExecutionInterrupt executionInterrupt : executionInterrupts){
                if(clean) {
                    breakpointRegistry.unregisterDebugInterrupt(executionInterrupt);
                }
                breakpointRegistry.registerDebugInterrupt(executionInterrupt);
            }
        }
    }

    private ExecutionInterrupts createExecutionDebugInterrupts(String executionId, InterruptType type) {
        ExecutionInterrupts executionInterrupts;
        executionInterrupts = new ExecutionInterrupts();
        executionInterrupts.setExecutionId(executionId);
        executionInterrupts.setCreatedTime(new Date(System.currentTimeMillis()));
        executionInterrupts.setType(type.name());
        return executionInterrupts;
    }

    private AbstractExecutionInterruptRegistry readDebugInterruptRegistry(String executionId, InterruptType type){
        ExecutionInterrupts executionInterrupts = repository.findByExecutionIdAndType(executionId, type.name());
        if(executionInterrupts == null){
            return null;
        }
        return serializationUtil.objFromBytes(executionInterrupts.getExecutionInterruptRegistry());
    }
}
