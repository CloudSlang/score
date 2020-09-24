package io.cloudslang.worker.monitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PerfMonitorCollectorImpl implements PerfMetricCollector {

    List<WorkerPerfMetric> workerPerfMetrics;

    public PerfMonitorCollectorImpl() {
        createMetrics();
    }

    private void createMetrics() {

    }

    @Override
    public Map<String, Double> collectMetric() {
        Map<String, Double> currentValues = new HashMap<>();
        for (WorkerPerfMetric metric :
                workerPerfMetrics) {
            currentValues.putAll(metric.measure());
        }
        return currentValues;
    }
}
