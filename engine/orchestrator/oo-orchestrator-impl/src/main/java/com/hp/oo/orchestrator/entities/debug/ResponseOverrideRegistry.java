package com.hp.oo.orchestrator.entities.debug;


import com.hp.oo.execution.debug.ExecutionInterrupt;
import com.hp.oo.execution.debug.ResponseOverride;

public class ResponseOverrideRegistry extends AbstractExecutionInterruptRegistry {

    private static final long serialVersionUID = 2324574569957935169L;
    private final ResponseOverride universalOverride ;

    public ResponseOverrideRegistry() {
        super();
        // we don't leak "this" here, because we're the only one that return the universal override reference.
        this.universalOverride = ResponseOverride.UNIVERSAL_OVERRIDE_RESPONSES;
    }

//    private  static ResponseOverride getUniversalResponseOverride() {
//        ResponseOverride override = new ResponseOverride(UNIVERSAL_OVERRIDE_UUID,null, null) {
//            private static final long serialVersionUID = -4898635114015341235L;
//            @Override
//            public synchronized boolean isPrompt() {
//                return true;
//            };
//        };
//
//        return override;
//    }

//    public boolean stepHasResponseOverride(String stepUuid) {
//        return stepHasDebugInterrupt(stepUuid);
//    }
//
//    public ResponseOverride getStepResponseOverride(String stepUuid) {
//        return (ResponseOverride) getStepDebugInterrupt(stepUuid);
//    }

    public void registerResponseOverride(ResponseOverride fr) {
        registerDebugInterrupt(fr);
    }

    public void unregisterResponseOverride(ResponseOverride fr) {
        unregisterDebugInterrupt(fr);
    }

    @Override
    public ExecutionInterrupt getUniversalInterrupt() {
        return universalOverride;
    }
}
