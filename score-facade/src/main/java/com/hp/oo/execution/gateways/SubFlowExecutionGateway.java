package com.hp.oo.execution.gateways;

import com.hp.oo.internal.sdk.execution.Execution;

/**
 * Created by IntelliJ IDEA.
 * User: peerme
 * Date: 2/12/12
 * Time: 10:22 AM
 */
//todo - remove - non blocking
public interface SubFlowExecutionGateway {
     void addExecution(Execution execution);
}
