package com.hp.oo.orchestrator.services;


import com.hp.oo.execution.debug.Breakpoint;
import com.hp.oo.execution.debug.ExecutionInterrupt;
import com.hp.oo.execution.debug.ResponseOverride;
import com.hp.oo.execution.services.ExecutionInterruptsService;
import com.hp.score.engine.data.DataBaseDetector;
import com.hp.score.engine.data.SqlUtils;
import junit.framework.Assert;
import org.codehaus.jackson.map.ObjectMapper;
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

import static com.hp.oo.execution.debug.ExecutionInterrupt.InterruptType;

/**
 * Created with IntelliJ IDEA.
 * User: Shehab Hajyhia
 * Date: 30/12/12
 * Time: 11:24
 */
@SuppressWarnings({"SpringContextConfigurationInspection"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
public class ExecutionInterruptsRegistryServiceTest {

    private static final String FLOW_UUID = "flow_uuid";
    private static final String STEP_UUID = "step_uuid";

    private static final String RESOLVED = "RESOLVED";
    private static final String ERROR = "ERROR";

    @Autowired
    ExecutionInterruptsSerializationUtil serializationUtil;

    @Autowired
    ExecutionInterruptsService service;

    @Autowired
    private ObjectMapper mapper;


    @Test
    public void testCreate(){

        String executionId = UUID.randomUUID().toString();
        Set<ExecutionInterrupt> executionInterrupts = new HashSet<>(2);
        ExecutionInterrupt executionInterrupt_actual_1 = createBreakpoint("flow_1","step_1",true);
        ExecutionInterrupt executionInterrupt_actual_2 = createBreakpoint("flow_1","step_2",true);
        executionInterrupts.add(executionInterrupt_actual_1);
        executionInterrupts.add(executionInterrupt_actual_2);
        Long id = service.createExecutionBreakpointInterrupts(executionId, executionInterrupts);
        Assert.assertNotNull(id);

        ExecutionInterrupt executionInterrupt_expected_1 = service.readExecutionDebugInterrupts(executionId, InterruptType.BREAKPOINT,STEP_UUID, "step_1");
        Assert.assertEquals(executionInterrupt_actual_1.getUUID(), executionInterrupt_expected_1.getUUID());
    }


    @Test
    public void testCreateWithJson(){

        String executionId = UUID.randomUUID().toString();
        Breakpoint debugInterrupt_actual_1 = (Breakpoint)createBreakpoint("flow_1","step_1",true);
        Breakpoint debugInterrupt_actual_2 = (Breakpoint)createBreakpoint("flow_1","step_2",true);
        Set<Breakpoint> debugInterrupts = new HashSet<>(2);
        debugInterrupts.add(debugInterrupt_actual_1);
        debugInterrupts.add(debugInterrupt_actual_2);

        Map<String,String> map = new HashMap<>(1);
        try {
            String interruptsAsString = mapper.writeValueAsString(debugInterrupts);
            map.put(InterruptType.BREAKPOINT.name(),interruptsAsString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Long id = service.createExecutionDebugInterrupts(executionId, map);
        Assert.assertNotNull(id);

        ExecutionInterrupt executionInterrupt_expected_1 = service.readExecutionDebugInterrupts(executionId,InterruptType.BREAKPOINT,STEP_UUID, "step_1");
        Assert.assertEquals(debugInterrupt_actual_1.getUUID(), executionInterrupt_expected_1.getUUID());


    }
    @Test
    public void testCreateInterruptsWithEnableDisable(){

        String executionId = UUID.randomUUID().toString();
        Set<ExecutionInterrupt> executionInterrupts = new HashSet<>(2);
        ExecutionInterrupt executionInterrupt_actual_1 = createBreakpoint("flow_1","step_1",true);
        ExecutionInterrupt executionInterrupt_actual_2 = createBreakpoint("flow_1","step_2",true);
        executionInterrupts.add(executionInterrupt_actual_1);
        executionInterrupts.add(executionInterrupt_actual_2);
        Long id = service.createExecutionBreakpointInterrupts(executionId, executionInterrupts);
        Assert.assertNotNull(id);

        ExecutionInterrupt executionInterrupt_expected_1 = service.readExecutionDebugInterrupts(executionId,InterruptType.BREAKPOINT,STEP_UUID, "step_1");
        Assert.assertEquals(executionInterrupt_actual_1.getUUID(), executionInterrupt_expected_1.getUUID());

        executionInterrupts = new HashSet<>(1);
        executionInterrupt_actual_1.setEnabled(false);
        executionInterrupts.add(executionInterrupt_actual_1);
        Long id_disabled = service.createExecutionBreakpointInterrupts(executionId, executionInterrupts);
        Assert.assertNotNull(id_disabled);
        Assert.assertEquals(id, id_disabled);

        ExecutionInterrupt executionInterrupt_expected_1_disabled = service.readExecutionDebugInterrupts(executionId,InterruptType.BREAKPOINT,STEP_UUID, "step_1");
        Assert.assertEquals(executionInterrupt_actual_1.getUUID(), executionInterrupt_expected_1_disabled.getUUID());
        Assert.assertEquals(executionInterrupt_expected_1.getUUID(), executionInterrupt_expected_1_disabled.getUUID());
        //assert disabled
        Assert.assertEquals(false, executionInterrupt_expected_1_disabled.isEnabled());



    }

    @Test
    public void testDelete(){
        String executionId = UUID.randomUUID().toString();
        Set<ExecutionInterrupt> executionInterrupts = new HashSet<>(2);
        ExecutionInterrupt executionInterrupt_actual_1 = createBreakpoint("flow_1","step_1",true);
        ExecutionInterrupt executionInterrupt_actual_2 = createBreakpoint("flow_1","step_2",true);
        executionInterrupts.add(executionInterrupt_actual_1);
        executionInterrupts.add(executionInterrupt_actual_2);
        Long id = service.createExecutionBreakpointInterrupts(executionId, executionInterrupts);
        Assert.assertNotNull(id);

        ExecutionInterrupt executionInterrupt_expected_1 = service.readExecutionDebugInterrupts(executionId,InterruptType.BREAKPOINT,STEP_UUID, "step_1");
        Assert.assertEquals(executionInterrupt_actual_1.getUUID(), executionInterrupt_expected_1.getUUID());

        executionInterrupts.remove(executionInterrupt_actual_1);
        Long id_ = service.createExecutionBreakpointInterrupts(executionId, executionInterrupts);
        Assert.assertNotNull(id_);
        Assert.assertEquals(id, id_);
        ExecutionInterrupt executionInterrupt_expected_2 = service.readExecutionDebugInterrupts(executionId,InterruptType.BREAKPOINT,STEP_UUID, "step_2");
        Assert.assertEquals(executionInterrupt_actual_2.getUUID(), executionInterrupt_expected_2.getUUID());


        service.removeExecutionDebugInterrupts(executionId);
        service.readExecutionDebugInterrupts(executionId,InterruptType.BREAKPOINT,STEP_UUID, "step_2");
    }

    @Test
    public void testCreateOverrideResponse(){
        String executionId = UUID.randomUUID().toString();
        Set<ExecutionInterrupt> executionInterrupts = new HashSet<>(2);
        ExecutionInterrupt executionInterrupt_actual_1 = createOverrideResponse("flow_1","step_1",true,false,RESOLVED);
        ExecutionInterrupt executionInterrupt_actual_2 = createOverrideResponse("flow_1", "step_2", true, false, ERROR);
        ExecutionInterrupt executionInterrupt_actual_3 = createOverrideResponse("flow_1", "step_3", true, true, RESOLVED);
        executionInterrupts.add(executionInterrupt_actual_1);
        executionInterrupts.add(executionInterrupt_actual_2);
        executionInterrupts.add(executionInterrupt_actual_3);

        Long id = service.createExecutionResponsesOverrideInterrupts(executionId, executionInterrupts);
        Assert.assertNotNull(id);

        ExecutionInterrupt executionInterrupt_expected_1 = service.readExecutionDebugInterrupts(executionId, InterruptType.OVERRIDE_RESPONSES,STEP_UUID, "step_1");
        Assert.assertEquals(executionInterrupt_actual_1.getUUID(), executionInterrupt_expected_1.getUUID());
        Assert.assertEquals("flow_1", executionInterrupt_expected_1.getValue(FLOW_UUID));
        Assert.assertEquals("step_1", executionInterrupt_expected_1.getValue(STEP_UUID));
        Assert.assertEquals(true, executionInterrupt_expected_1.isEnabled());
        Assert.assertEquals(false,((ResponseOverride) executionInterrupt_expected_1).isPrompt());
        Assert.assertEquals(RESOLVED,((ResponseOverride) executionInterrupt_expected_1).getResponse());


        ExecutionInterrupt executionInterrupt_expected_2 = service.readExecutionDebugInterrupts(executionId, InterruptType.OVERRIDE_RESPONSES,STEP_UUID, "step_2");
        Assert.assertEquals(executionInterrupt_actual_2.getUUID(), executionInterrupt_expected_2.getUUID());
        Assert.assertEquals("flow_1", executionInterrupt_expected_2.getValue(FLOW_UUID));
        Assert.assertEquals("step_2", executionInterrupt_expected_2.getValue(STEP_UUID));
        Assert.assertEquals(true, executionInterrupt_expected_2.isEnabled());
        Assert.assertEquals(false,((ResponseOverride) executionInterrupt_expected_2).isPrompt());
        Assert.assertEquals(ERROR,((ResponseOverride) executionInterrupt_expected_2).getResponse());


        ExecutionInterrupt executionInterrupt_expected_3 = service.readExecutionDebugInterrupts(executionId,InterruptType.OVERRIDE_RESPONSES, STEP_UUID, "step_3");
        Assert.assertEquals(executionInterrupt_actual_3.getUUID(), executionInterrupt_expected_3.getUUID());
        Assert.assertEquals("flow_1", executionInterrupt_expected_3.getValue(FLOW_UUID));
        Assert.assertEquals("step_3", executionInterrupt_expected_3.getValue(STEP_UUID));
        Assert.assertEquals(true, executionInterrupt_expected_3.isEnabled());
        Assert.assertEquals(true,((ResponseOverride) executionInterrupt_expected_3).isPrompt());
        Assert.assertEquals(RESOLVED,((ResponseOverride) executionInterrupt_expected_3).getResponse());

    }

    @Test
    public void testOverrideResponseAll(){
        String executionId = UUID.randomUUID().toString();
        Set<ExecutionInterrupt> executionInterrupts = new HashSet<>(2);
        ExecutionInterrupt executionInterrupt_actual_1 = createOverrideResponse("flow_1","step_1",true, false, RESOLVED);
        ExecutionInterrupt executionInterrupt_actual_2 = createOverrideResponse("flow_1","step_2",true, true , ERROR);
        executionInterrupts.add(executionInterrupt_actual_1);
        executionInterrupts.add(executionInterrupt_actual_2);

        Long id = service.createExecutionResponsesOverrideInterrupts(executionId, executionInterrupts);
        Assert.assertNotNull(id);

        ExecutionInterrupt executionInterrupt_expected_1 = service.readExecutionDebugInterrupts(executionId,InterruptType.OVERRIDE_RESPONSES,STEP_UUID, "step_1");
        Assert.assertEquals(executionInterrupt_actual_1.getUUID(), executionInterrupt_expected_1.getUUID());
        Assert.assertEquals("flow_1", executionInterrupt_expected_1.getValue(FLOW_UUID));
        Assert.assertEquals("step_1", executionInterrupt_expected_1.getValue(STEP_UUID));
        Assert.assertEquals(true, executionInterrupt_expected_1.isEnabled());
        Assert.assertEquals(false,((ResponseOverride) executionInterrupt_expected_1).isPrompt());
        Assert.assertEquals(RESOLVED,((ResponseOverride) executionInterrupt_expected_1).getResponse());


        Long id_true = service.setExecutionOverrideAllResponses(executionId,true);
        Assert.assertNotNull(id_true);

        ExecutionInterrupt executionInterrupt_expected_universal = service.readExecutionDebugInterrupts(executionId,InterruptType.OVERRIDE_RESPONSES, STEP_UUID, "step_1");
        Assert.assertEquals("d4fbb32f-7289-4984-be06-e709e1909b6a", executionInterrupt_expected_universal.getUUID());
        Assert.assertEquals("*", executionInterrupt_expected_universal.getValue("*"));
        Assert.assertEquals(true, executionInterrupt_expected_universal.isEnabled());
        Assert.assertEquals(true,((ResponseOverride) executionInterrupt_expected_universal).isPrompt());

        ExecutionInterrupt executionInterrupt_actual_3 = createOverrideResponse("flow_1","step_3",false, false,ERROR);
        executionInterrupts.add(executionInterrupt_actual_3);
        Long id_3 = service.createExecutionResponsesOverrideInterrupts(executionId, executionInterrupts);
        Assert.assertNotNull(id_3);

        executionInterrupt_expected_universal = service.readExecutionDebugInterrupts(executionId,InterruptType.OVERRIDE_RESPONSES, STEP_UUID, "step_3");
        Assert.assertEquals("d4fbb32f-7289-4984-be06-e709e1909b6a", executionInterrupt_expected_universal.getUUID());
        Assert.assertEquals("*", executionInterrupt_expected_universal.getValue("*"));
        Assert.assertEquals(true, executionInterrupt_expected_universal.isEnabled());
        Assert.assertEquals(true,((ResponseOverride) executionInterrupt_expected_universal).isPrompt());
    }


    private ExecutionInterrupt createBreakpoint(String flowUuid, String stepUuid, boolean enabled) {
        Map<String,String> interruptMetadata = new HashMap<>(2);
        interruptMetadata.put(FLOW_UUID, flowUuid);
        interruptMetadata.put(STEP_UUID, stepUuid);

        Breakpoint breakpoint = new Breakpoint(interruptMetadata);
        breakpoint.setEnabled(enabled);


        return breakpoint;
    }

    private ExecutionInterrupt createOverrideResponse(String flowUuid, String stepUuid, boolean enabled,boolean prompt, String response) {
        Map<String,String> interruptMetadata = new HashMap<>(2);
        interruptMetadata.put(FLOW_UUID,flowUuid);
        interruptMetadata.put(STEP_UUID,stepUuid);

        ResponseOverride responseOverride = new ResponseOverride(interruptMetadata,response);
        responseOverride.setEnabled(enabled);
        responseOverride.setPrompt(prompt);
        return responseOverride;
    }


    @Configuration
    @EnableJpaRepositories("com.hp.oo.orchestrator")
    @EnableTransactionManagement
    @ImportResource("META-INF/spring/orchestratorEmfContext.xml")
    static class Configurator {

        @Bean
        public ExecutionInterruptsService executionDebugInterruptsService(){
            return new ExecutionInterruptsServiceImpl();
        }

        @Bean
        public ExecutionInterruptsSerializationUtil execDebugInterruptsSerializationUtil(){
            return new ExecutionInterruptsSerializationUtil();
        }

        @Bean
        public ObjectMapper getObjectMapper() {
            return new ObjectMapper();
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
