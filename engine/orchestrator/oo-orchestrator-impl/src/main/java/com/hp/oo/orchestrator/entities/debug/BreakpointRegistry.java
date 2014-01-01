package com.hp.oo.orchestrator.entities.debug;

import com.hp.oo.execution.debug.Breakpoint;
import com.hp.oo.execution.debug.ExecutionInterrupt;

import static com.google.common.base.Preconditions.checkArgument;


public class BreakpointRegistry extends AbstractExecutionInterruptRegistry {

    private static final long serialVersionUID = -1953808763718808282L;

    public BreakpointRegistry() {
        super();
    }



//    public boolean stepHasBreakpoint(String stepUuid) {
//        return stepHasDebugInterrupt(stepUuid);
//    }
//
//    public Breakpoint getStepBreakpoint(String stepUuid) {
//        return (Breakpoint) getStepDebugInterrupt(stepUuid);
//    }

    public void registerBreakpoint(Breakpoint bp) {
        registerDebugInterrupt(bp);
    }

    public void unregisterBreakpoint(Breakpoint bp) {
        unregisterDebugInterrupt(bp);
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public ExecutionInterrupt getUniversalInterrupt() {
        return Breakpoint.UNIVERSAL_BREAKPOINT;
    }

}
