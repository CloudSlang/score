package com.hp.oo.orchestrator.repositories;

import com.hp.oo.execution.debug.Breakpoint;
import com.hp.oo.execution.debug.ExecutionInterrupt;
import com.hp.oo.execution.debug.ResponseOverride;
import com.hp.oo.orchestrator.entities.ExecutionInterrupts;
import com.hp.oo.orchestrator.entities.debug.AbstractExecutionInterruptRegistry;
import com.hp.oo.orchestrator.entities.debug.BreakpointRegistry;
import com.hp.oo.orchestrator.entities.debug.ResponseOverrideRegistry;
import com.hp.oo.orchestrator.services.ExecutionInterruptsSerializationUtil;
import com.hp.score.engine.data.DataBaseDetector;
import com.hp.score.engine.data.SqlUtils;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: shehab hajyhia
 * Date: 20/12/12
 * Time: 15:49
 */
@SuppressWarnings({"SpringContextConfigurationInspection"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
public class ExecutionInterruptsRepositoryTest {

    private static final String FLOW_UUID = "flow_uuid";
    private static final String STEP_UUID = "step_uuid";
    //RESOLVED, ERROR, DIAGNOSED, EXCEPTION
    private static final String RESOLVED = "RESOLVED";
    private static final String ERROR = "ERROR";
    @Autowired
    ExecutionInterruptsRepository repository;

    @Autowired
    ExecutionInterruptsSerializationUtil serializationUtil;

    @Test
    public void testSaveAndGetBreakpoint() throws IOException {

        AbstractExecutionInterruptRegistry breakpointRegistry_actual = new BreakpointRegistry();
        breakpointRegistry_actual.registerDebugInterrupt(createBreakpoint("flow_1", "step_1", true));
        breakpointRegistry_actual.registerDebugInterrupt(createBreakpoint("flow_1", "step_2", false));

        ExecutionInterrupts executionInterrupts = new ExecutionInterrupts();
        executionInterrupts.setExecutionId(UUID.randomUUID().toString());
        executionInterrupts.setCreatedTime(new Date(System.currentTimeMillis()));
        executionInterrupts.setType(ExecutionInterrupt.InterruptType.BREAKPOINT.name());
        executionInterrupts.setExecutionInterruptRegistry(serializationUtil.objToBytes(breakpointRegistry_actual));
        ExecutionInterrupts saved_1 = repository.save(executionInterrupts);
        Assert.assertNotNull(saved_1);

        ExecutionInterrupts find_1 = repository.findOne(saved_1.getId());
        Assert.assertNotNull(find_1);
        Assert.assertEquals(executionInterrupts.getCreatedTime(),find_1.getCreatedTime());

        Assert.assertNotNull(find_1.getExecutionInterruptRegistry());
        AbstractExecutionInterruptRegistry breakpointRegistry_expected = serializationUtil.objFromBytes(find_1.getExecutionInterruptRegistry());
        Assert.assertNotNull(breakpointRegistry_expected);

        ExecutionInterrupt executionInterrupt_expected_1 = breakpointRegistry_expected.getInterruptByKey(STEP_UUID, "step_1");
        ExecutionInterrupt executionInterrupt_expected_2 = breakpointRegistry_expected.getInterruptByKey(STEP_UUID, "step_2");

        Assert.assertNotNull(executionInterrupt_expected_1);
        Assert.assertNotNull(executionInterrupt_expected_2);

        ExecutionInterrupt executionInterrupt_actual_1 = breakpointRegistry_actual.getInterruptByKey(STEP_UUID, "step_1");
        ExecutionInterrupt executionInterrupt_actual_2 = breakpointRegistry_actual.getInterruptByKey(STEP_UUID, "step_2");

        Assert.assertEquals(executionInterrupt_actual_1.getUUID(), executionInterrupt_expected_1.getUUID());
        Assert.assertEquals(executionInterrupt_actual_2.getUUID(), executionInterrupt_expected_2.getUUID());
        Assert.assertEquals(executionInterrupt_actual_2.isEnabled(), executionInterrupt_expected_2.isEnabled());
        Assert.assertEquals(executionInterrupt_actual_2.isEnabled(), executionInterrupt_expected_2.isEnabled());


        executionInterrupt_expected_1 = breakpointRegistry_expected.getInterruptById(executionInterrupt_actual_1.getUUID());
        executionInterrupt_expected_2 = breakpointRegistry_expected.getInterruptById(executionInterrupt_actual_2.getUUID());

        Assert.assertNotNull(executionInterrupt_expected_1);
        Assert.assertNotNull(executionInterrupt_expected_2);

        Assert.assertEquals(executionInterrupt_actual_2.isEnabled(), executionInterrupt_expected_2.isEnabled());
        Assert.assertEquals(executionInterrupt_actual_2.isEnabled(), executionInterrupt_expected_2.isEnabled());


    }

    @Test
    public void testSaveAndGetResponseOverride() throws IOException {

        AbstractExecutionInterruptRegistry breakpointRegistry_actual = new ResponseOverrideRegistry();
        breakpointRegistry_actual.registerDebugInterrupt(createOverrideResponse("flow_1", "step_1", true, false, RESOLVED));
        breakpointRegistry_actual.registerDebugInterrupt(createOverrideResponse("flow_1", "step_2", false, false, ERROR));

        ExecutionInterrupts executionInterrupts = new ExecutionInterrupts();
        executionInterrupts.setExecutionId(UUID.randomUUID().toString());
        executionInterrupts.setCreatedTime(new Date(System.currentTimeMillis()));
        executionInterrupts.setType(ExecutionInterrupt.InterruptType.OVERRIDE_RESPONSES.name());
        executionInterrupts.setExecutionInterruptRegistry(serializationUtil.objToBytes(breakpointRegistry_actual));

        ExecutionInterrupts saved_1 = repository.save(executionInterrupts);
        Assert.assertNotNull(saved_1);

        ExecutionInterrupts find_1 = repository.findOne(saved_1.getId());
        Assert.assertNotNull(find_1);
        Assert.assertEquals(executionInterrupts.getCreatedTime(),find_1.getCreatedTime());

        Assert.assertNotNull(find_1.getExecutionInterruptRegistry());
        AbstractExecutionInterruptRegistry responseOverrideRegistry_expected = serializationUtil.objFromBytes(find_1.getExecutionInterruptRegistry());
        Assert.assertNotNull(responseOverrideRegistry_expected);

        ExecutionInterrupt executionInterrupt_expected_1 = responseOverrideRegistry_expected.getInterruptByKey(STEP_UUID, "step_1");
        ExecutionInterrupt executionInterrupt_expected_2 = responseOverrideRegistry_expected.getInterruptByKey(STEP_UUID, "step_2");

        Assert.assertNotNull(executionInterrupt_expected_1);
        Assert.assertNotNull(executionInterrupt_expected_2);

        ExecutionInterrupt executionInterrupt_actual_1 = breakpointRegistry_actual.getInterruptByKey(STEP_UUID, "step_1");
        ExecutionInterrupt executionInterrupt_actual_2 = breakpointRegistry_actual.getInterruptByKey(STEP_UUID, "step_2");

        Assert.assertEquals(executionInterrupt_actual_1.getUUID(), executionInterrupt_expected_1.getUUID());
        Assert.assertEquals(executionInterrupt_actual_2.getUUID(), executionInterrupt_expected_2.getUUID());
        Assert.assertEquals(executionInterrupt_actual_2.isEnabled(), executionInterrupt_expected_2.isEnabled());
        Assert.assertEquals(executionInterrupt_actual_2.isEnabled(), executionInterrupt_expected_2.isEnabled());

        executionInterrupt_expected_1 = responseOverrideRegistry_expected.getInterruptById(executionInterrupt_actual_1.getUUID());
        executionInterrupt_expected_2 = responseOverrideRegistry_expected.getInterruptById(executionInterrupt_actual_2.getUUID());

        Assert.assertNotNull(executionInterrupt_expected_1);
        Assert.assertNotNull(executionInterrupt_expected_2);

        Assert.assertEquals(executionInterrupt_actual_2.isEnabled(), executionInterrupt_expected_2.isEnabled());
        Assert.assertEquals(executionInterrupt_actual_2.isEnabled(), executionInterrupt_expected_2.isEnabled());

    }


    @Test
    public void testSaveAndGetResponseOverrideSetAll() throws IOException {

        AbstractExecutionInterruptRegistry responseOverrideRegistry_actual = new ResponseOverrideRegistry();
        responseOverrideRegistry_actual.registerDebugInterrupt(createOverrideResponse("flow_1", "step_1", true, false, RESOLVED));
        responseOverrideRegistry_actual.registerDebugInterrupt(createOverrideResponse("flow_1", "step_2", false, false, ERROR));
        responseOverrideRegistry_actual.setInterruptAll(true);

        ExecutionInterrupts executionInterrupts = new ExecutionInterrupts();
        executionInterrupts.setExecutionId(UUID.randomUUID().toString());
        executionInterrupts.setCreatedTime(new Date(System.currentTimeMillis()));
        executionInterrupts.setType(ExecutionInterrupt.InterruptType.OVERRIDE_RESPONSES.name());
        executionInterrupts.setExecutionInterruptRegistry(serializationUtil.objToBytes(responseOverrideRegistry_actual));

        ExecutionInterrupts saved_1 = repository.save(executionInterrupts);
        Assert.assertNotNull(saved_1);

        ExecutionInterrupts find_1 = repository.findOne(saved_1.getId());

        Assert.assertNotNull(find_1.getExecutionInterruptRegistry());
        AbstractExecutionInterruptRegistry responseOverrideRegistry_expected = serializationUtil.objFromBytes(find_1.getExecutionInterruptRegistry());
        Assert.assertNotNull(responseOverrideRegistry_expected);


        Assert.assertNotNull(responseOverrideRegistry_expected);
        Assert.assertNotNull(responseOverrideRegistry_expected.getInterruptByKey("*", "*"));
        Assert.assertNotNull(responseOverrideRegistry_expected.getInterruptById(responseOverrideRegistry_actual.getUniversalInterrupt().getUUID()));

    }

    @Test
    public void testSaveAndGetByExecutionId() throws IOException {

        AbstractExecutionInterruptRegistry responseOverrideRegistry_actual = new ResponseOverrideRegistry();
        responseOverrideRegistry_actual.registerDebugInterrupt(createOverrideResponse("flow_1", "step_1", true, false, RESOLVED));
        responseOverrideRegistry_actual.registerDebugInterrupt(createOverrideResponse("flow_1", "step_2", false, false, ERROR));
        responseOverrideRegistry_actual.setInterruptAll(true);

        ExecutionInterrupts executionInterrupts = new ExecutionInterrupts();
        executionInterrupts.setExecutionId(UUID.randomUUID().toString());
        executionInterrupts.setCreatedTime(new Date(System.currentTimeMillis()));
        executionInterrupts.setType(ExecutionInterrupt.InterruptType.OVERRIDE_RESPONSES.name());
        executionInterrupts.setExecutionInterruptRegistry(serializationUtil.objToBytes(responseOverrideRegistry_actual));

        ExecutionInterrupts saved_1 = repository.save(executionInterrupts);
        Assert.assertNotNull(saved_1);


        ExecutionInterrupts find_1 = repository.findByExecutionIdAndType(saved_1.getExecutionId(), ExecutionInterrupt.InterruptType.OVERRIDE_RESPONSES.name());

        Assert.assertNotNull(find_1.getExecutionInterruptRegistry());

    }

    @Test
    public void testSaveAndGetTwice() throws IOException {

        AbstractExecutionInterruptRegistry responseOverrideRegistry_actual = new ResponseOverrideRegistry();
        responseOverrideRegistry_actual.registerDebugInterrupt(createOverrideResponse("flow_1", "step_1", true, false, RESOLVED));
        responseOverrideRegistry_actual.registerDebugInterrupt(createOverrideResponse("flow_1", "step_2", false, false, ERROR));
        responseOverrideRegistry_actual.setInterruptAll(true);

        ExecutionInterrupts executionInterrupts = new ExecutionInterrupts();
        executionInterrupts.setExecutionId(UUID.randomUUID().toString());
        executionInterrupts.setCreatedTime(new Date(System.currentTimeMillis()));
        executionInterrupts.setType(ExecutionInterrupt.InterruptType.OVERRIDE_RESPONSES.name());
        executionInterrupts.setExecutionInterruptRegistry(serializationUtil.objToBytes(responseOverrideRegistry_actual));

        ExecutionInterrupts saved_1 = repository.save(executionInterrupts);
        Assert.assertNotNull(saved_1);

        responseOverrideRegistry_actual.setInterruptAll(false);
        executionInterrupts.setExecutionInterruptRegistry(serializationUtil.objToBytes(responseOverrideRegistry_actual));
        ExecutionInterrupts saved_2 = repository.save(executionInterrupts);
        Assert.assertNotNull(saved_2);
        Assert.assertEquals(saved_1.getId(), saved_2.getId());
        Assert.assertEquals(saved_1.getExecutionId(),saved_2.getExecutionId());

        Assert.assertEquals(1, repository.findAll().size());

        AbstractExecutionInterruptRegistry responseOverrideRegistry_expected = serializationUtil.objFromBytes(saved_1.getExecutionInterruptRegistry());
        responseOverrideRegistry_actual = serializationUtil.objFromBytes(saved_2.getExecutionInterruptRegistry());

        Assert.assertEquals(false ,responseOverrideRegistry_actual.isInterruptAll());
        Assert.assertEquals(responseOverrideRegistry_expected.size() ,responseOverrideRegistry_actual.size());
    }




    @Test
    public void testSaveAndDelete() throws IOException {

        String executionId = UUID.randomUUID().toString();
        AbstractExecutionInterruptRegistry overrideResponseRegistry_actual = new ResponseOverrideRegistry();
        overrideResponseRegistry_actual.registerDebugInterrupt(createOverrideResponse("flow_1", "step_1", true, false, RESOLVED));
        overrideResponseRegistry_actual.registerDebugInterrupt(createOverrideResponse("flow_1", "step_2", false, false, ERROR));
        overrideResponseRegistry_actual.setInterruptAll(true);

        ExecutionInterrupts executionInterrupts = new ExecutionInterrupts();
        executionInterrupts.setExecutionId(executionId);
        executionInterrupts.setCreatedTime(new Date(System.currentTimeMillis()));
        executionInterrupts.setType(ExecutionInterrupt.InterruptType.OVERRIDE_RESPONSES.name());
        executionInterrupts.setExecutionInterruptRegistry(serializationUtil.objToBytes(overrideResponseRegistry_actual));

        ExecutionInterrupts saved_1 = repository.save(executionInterrupts);
        Assert.assertNotNull(saved_1);

        AbstractExecutionInterruptRegistry breakpointRegistry_actual = new BreakpointRegistry();
        breakpointRegistry_actual.registerDebugInterrupt(createBreakpoint("flow_1", "step_1", true));
        breakpointRegistry_actual.registerDebugInterrupt(createBreakpoint("flow_1", "step_2", false));

        ExecutionInterrupts executionInterrupts_ = new ExecutionInterrupts();
        executionInterrupts_.setExecutionId(executionId);
        executionInterrupts_.setCreatedTime(new Date(System.currentTimeMillis()));
        executionInterrupts_.setType(ExecutionInterrupt.InterruptType.BREAKPOINT.name());
        executionInterrupts_.setExecutionInterruptRegistry(serializationUtil.objToBytes(breakpointRegistry_actual));
        ExecutionInterrupts saved_2 = repository.save(executionInterrupts_);
        Assert.assertNotNull(saved_2);


        List<ExecutionInterrupts> all = repository.findByExecutionId(executionId);
        Assert.assertEquals(2, all.size());
        repository.delete(all);
        Assert.assertEquals(0,repository.findByExecutionId(executionId).size());



    }

    /**
     *
     * @param flowUuid
     * @param stepUuid
     * @param enabled
     * @return
     */
    private ExecutionInterrupt createBreakpoint(String flowUuid, String stepUuid, boolean enabled) {
        Map<String,String> interruptMetadata = new HashMap<>(2);
        interruptMetadata.put(FLOW_UUID, flowUuid);
        interruptMetadata.put(STEP_UUID, stepUuid);

        Breakpoint breakpoin = new Breakpoint(interruptMetadata);
        breakpoin.setEnabled(enabled);


        return breakpoin;
    }

    /**
     *
     * @param flowUuid
     * @param stepUuid
     * @param enabled
     * @param prompt
     * @param response RESOLVED, ERROR, DIAGNOSED, EXCEPTION
     * @return
     */
    private ExecutionInterrupt createOverrideResponse(String flowUuid, String stepUuid, boolean enabled,boolean prompt, String response) {
        Map<String,String> interruptMetadata = new HashMap<>(2);
        interruptMetadata.put(FLOW_UUID,flowUuid);
        interruptMetadata.put(STEP_UUID,stepUuid);

        ResponseOverride responseOverride = new ResponseOverride(interruptMetadata,response);
        responseOverride.setEnabled(prompt);
        responseOverride.setPrompt(false);
        return responseOverride;
    }


    @Configuration
    @EnableJpaRepositories("com.hp.oo.orchestrator")
    @EnableTransactionManagement
    @ImportResource("META-INF/spring/orchestratorEmfContext.xml")
    static class Configurator{
        @Bean
        public ExecutionInterruptsSerializationUtil execDebugInterruptsSerializationUtil(){
            return new ExecutionInterruptsSerializationUtil();
        }

	    @Bean
	    SqlUtils sqlUtils() {
		    return new SqlUtils();
	    }

	    @Bean
	    DataBaseDetector dataBaseDetector() {
		    return new DataBaseDetector();
	    }
    }
}
