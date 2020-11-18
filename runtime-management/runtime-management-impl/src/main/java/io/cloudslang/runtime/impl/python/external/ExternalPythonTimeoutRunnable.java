package io.cloudslang.runtime.impl.python.external;


import java.util.concurrent.ConcurrentMap;

public class ExternalPythonTimeoutRunnable implements Runnable {

    private final long uniqueKey;
    private final ConcurrentMap<Long, Object> map;

    public ExternalPythonTimeoutRunnable(long uniqueKey, ConcurrentMap<Long, Object> map) {
        this.uniqueKey = uniqueKey;
        this.map = map;
    }

    @Override
    public void run() {
        Object value = map.remove(uniqueKey);
        if (value instanceof Process) {
            Process process = (Process) value;
            map.put(uniqueKey, Boolean.TRUE);
            try {
                process.destroy();
            } catch (Exception ignore) {
            }
        }
    }
}
