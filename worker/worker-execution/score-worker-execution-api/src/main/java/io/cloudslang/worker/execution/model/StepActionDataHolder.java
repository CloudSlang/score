package io.cloudslang.worker.execution.model;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class StepActionDataHolder {

    protected final List<Map<String, ?>> holder;

    public StepActionDataHolder() {
        this.holder = new ArrayList<>(4);
    }

    // Used by subclasses only
    private StepActionDataHolder(StepActionDataHolder stepActionDataHolder) {
        this.holder = stepActionDataHolder.holder;
    }

    public void addNotNullPartToHolder(Map<String, ?> actionDataPart) {
        this.holder.add(actionDataPart);
    }

    public void addNullablePartToHolder(Map<String, ?> actionDataPart) {
        if (actionDataPart != null) {
            this.holder.add(actionDataPart);
        }
    }

    public static class ReadonlyStepActionDataAccessor extends StepActionDataHolder {

        public ReadonlyStepActionDataAccessor(StepActionDataHolder stepActionDataHolder) {
            super(stepActionDataHolder);
        }

        public void addNotNullPartToHolder(Map<String, ?> actionDataPart) {
            throw new UnsupportedOperationException("Cannot mutate ReadonlyStepActionDataAccessor");
        }

        public void addNullablePartToHolder(Map<String, ?> actionDataPart) {
            throw new UnsupportedOperationException("Cannot mutate ReadonlyStepActionDataAccessor");
        }

        public Object getValue(String key) {
            ListIterator<Map<String, ?>> listIterator = this.holder.listIterator(this.holder.size());
            while (listIterator.hasPrevious()) {
                Map<String, ?> element = listIterator.previous();
                if (element.containsKey(key)) {
                    return element.get(key);
                }
            }
            return null;
        }
    }


}
