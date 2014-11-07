package com.hp.score.samples.docker;

import com.hp.score.api.TriggeringProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hp.score.samples.openstack.OpenstackCommons.*;
import static com.hp.score.samples.openstack.OpenstackCommons.EXECUTE_METHOD;
import static com.hp.score.samples.openstack.OpenstackCommons.SSH_CLASS;

import com.hp.score.samples.openstack.actions.*;


/**
 * Date: 10/23/2014
 *
 * @author lesant
 */

public class SSHFlow {
    private List<InputBinding> inputBindings;

    public SSHFlow() {
        inputBindings = generateInitialInputBindings();
    }

    private List<InputBinding> generateInitialInputBindings() {
        List<InputBinding> bindings = new ArrayList<>(7);

        bindings.add(InputBindingFactory.createInputBinding("Username", USERNAME_KEY, true));
        bindings.add(InputBindingFactory.createInputBinding("Password", PASSWORD_KEY, true));
        bindings.add(InputBindingFactory.createInputBinding("Connection IP", CONNECTION_IP_KEY, true));
        bindings.add(InputBindingFactory.createInputBindingWithDefaultValue("Port", PORT_KEY, true, "22"));
        bindings.add(InputBindingFactory.createInputBinding("SSH Command", COMMAND_KEY, true));
        return bindings;
    }

    public TriggeringProperties createSSHFlow() {
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

        Long sshStepId = 0L;
        Long successStepId = 1L;
        Long failureStepId = 2L;


        createSSHStep(builder, sshStepId, successStepId, failureStepId);


        createSuccessStep(builder, successStepId);

        createFailureStep(builder, failureStepId);

        Map<String, Serializable> context = new HashMap<>();
        context.put(FLOW_DESCRIPTION, "SSH action");
        builder.setInitialExecutionContext(context);

        builder.setBeginStep(0L);

        return builder.createTriggeringProperties();
    }

    private void createSSHStep(ExecutionPlanBuilder builder, Long stepId, Long successStepId, Long failureStepId){
        List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>(2);

        navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, RETURN_CODE, SUCCESS_CODE, successStepId));
        navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, failureStepId));

        builder.addOOActionStep(stepId, SSH_CLASS, EXECUTE_METHOD, null, navigationMatchers);
    }

    public List<InputBinding> getInputBindings() {
        return inputBindings;
    }
}
