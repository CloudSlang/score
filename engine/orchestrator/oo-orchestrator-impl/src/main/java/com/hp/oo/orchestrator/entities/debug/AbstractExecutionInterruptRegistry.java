package com.hp.oo.orchestrator.entities.debug;

import com.hp.oo.execution.debug.ExecutionInterrupt;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class AbstractExecutionInterruptRegistry implements  Serializable {

    private static final long serialVersionUID = 6866301694627260683L;
    // these are crudely made thread safe via synchronized methods.
    protected Map<String, ExecutionInterrupt> interrupts;

    private AtomicBoolean interruptAll = new AtomicBoolean();

    protected AbstractExecutionInterruptRegistry() {
        interrupts = new HashMap<>();
    }

//    @Override
    public synchronized Collection<ExecutionInterrupt> clearAll() {

        List<ExecutionInterrupt> result = new ArrayList<>(interrupts.values());
        interrupts.clear();
//        interruptAll.set(false);
        return result;
    }

//    @Override
    public synchronized ExecutionInterrupt getInterruptById(String interruptId) {
        if (this.isInterruptAll()) {
            ExecutionInterrupt universalInterrupt = getUniversalInterrupt();
            if (universalInterrupt.getUUID().equals(interruptId)) {
                return universalInterrupt;
            }
        }

        if(interruptId == null){
            throw new RuntimeException("interrupt id is null");
        }
        return interrupts.get(interruptId);
    }

    public abstract ExecutionInterrupt getUniversalInterrupt();

    public synchronized ExecutionInterrupt getInterruptByKey(String key, String value){

        if (this.isInterruptAll()) {
            ExecutionInterrupt universalInterrupt = getUniversalInterrupt();
            if (universalInterrupt.getValue(key)!=null && universalInterrupt.getValue(key).equals(value)) {
                return universalInterrupt;
            }
        }
        for (ExecutionInterrupt interrupt : interrupts.values()){
            if(interrupt.getValue(key)!=null && interrupt.getValue(key).equals(value) ){
                return  interrupt;
            }
        }
        return null;
    }

//    @Override
    public synchronized boolean registerDebugInterrupt(ExecutionInterrupt bp) {
        if (bp == null) {
            return false;
        }

        if (interrupts.containsKey(bp.getUUID())) {
            return false;
        }

        interrupts.put(bp.getUUID(), bp);
        return true;
    }

//    @Override
    public synchronized boolean unregisterDebugInterrupt(ExecutionInterrupt bp) {
        if (bp == null) {
            return false;
        }

        ExecutionInterrupt foundInterrupt = interrupts.remove(bp.getUUID());
        return (foundInterrupt != null);
    }

//    @Override
    public void disableAll() {
        bulkSetEnabled(false);
    }

//    @Override
    public void enableAll() {
        bulkSetEnabled(true);
    }

    protected synchronized void bulkSetEnabled(boolean enabled) {
        doBulkSetEnabled(enabled, this.interrupts.values());
    }

    protected void doBulkSetEnabled(boolean enabled, Collection<ExecutionInterrupt> interrupts) {
        if (interrupts == null || interrupts.isEmpty()) {
            return;
        }
        for (ExecutionInterrupt interrupt : interrupts) {
            interrupt.setEnabled(enabled);

        }
    }

    public boolean setInterruptAll(boolean interruptAll) {
        boolean oldVal = this.interruptAll.getAndSet(interruptAll);

        return oldVal;
    }

    public boolean isInterruptAll() {
        return interruptAll.get();
    }

//    @Override
    public int size() {
        return interrupts.size();
    }
}
