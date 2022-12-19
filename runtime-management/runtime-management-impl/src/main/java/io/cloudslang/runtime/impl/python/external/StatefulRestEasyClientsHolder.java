/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cloudslang.runtime.impl.python.external;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.conn.ClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

@Component("statefulRestEasyClientsHolder")
@SuppressWarnings("deprecation")
public class StatefulRestEasyClientsHolder {

    private static final int MAX_CONNECTIONS = 60;

    private static final long TIMER_TASK_DELAY_MILLIS = 30_000L;

    private static final String TIMER_THREAD_NAME = "restEasyClientTimer";

    private final RestEasyClientWrapper restEasyClientWrapper;

    private final ResteasyClient restEasyClient;

    private final Object cleanupLock;
    private final HashMap<String, HttpPoolCleanupTask> cleanupTaskMap;
    private final HashMap<String, Timer> timerMap;

    @Autowired
    public StatefulRestEasyClientsHolder(RestEasyClientWrapper restEasyClientWrapper) {
        this.cleanupLock = new Object();
        this.cleanupTaskMap = new HashMap<>();
        this.timerMap = new HashMap<>();
        this.restEasyClientWrapper = restEasyClientWrapper;

        restEasyClient = generateClientForInitialization(TIMER_THREAD_NAME,
                MAX_CONNECTIONS,
                MAX_CONNECTIONS
        );
    }

    @PostConstruct
    public void init() {
        // Cannot start thread in constructor to not have "this" reference escape
        synchronized (cleanupLock) {
            for (Map.Entry<String, HttpPoolCleanupTask> entry : cleanupTaskMap.entrySet()) {
                final Timer timer = new Timer(entry.getKey(), false);
                // To periodically refresh the connection pool (close idle and expired connections)
                // Start first cleanup 3 minutes after startup
                timer.scheduleAtFixedRate(entry.getValue(), 6 * TIMER_TASK_DELAY_MILLIS, TIMER_TASK_DELAY_MILLIS);
                timerMap.put(entry.getKey(), timer);
            }
        }
    }

    @PreDestroy
    public void preDestroy() {
        doCloseRestEasyClient();
    }

    public ResteasyClient getRestEasyClient() {
        return restEasyClient;
    }

    public void closeRestEasyClient() {
        doCloseRestEasyClient();
    }

    //<editor-fold desc="Internal methods">
    private ResteasyClient generateClientForInitialization(String name, int maxConnections, int maxPerRoute) {
        try {
            final Pair<ResteasyClient, ClientConnectionManager> pairClientConnectionManager = restEasyClientWrapper.
                    getClientForRemoteConfiguration(maxConnections, maxPerRoute);
            synchronized (cleanupLock) {
                cleanupTaskMap.put(name, new HttpPoolCleanupTask(pairClientConnectionManager.getRight()));
            }
            return pairClientConnectionManager.getLeft();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new RuntimeException("Could not create stateful rest easy client: ", e);
        }
    }

    private void doCloseRestEasyClient() {
        restEasyClient.close();
        doCloseTimerByKey(TIMER_THREAD_NAME);
    }

    private void doCloseTimerByKey(String key) {
        synchronized (cleanupLock) {
            cleanupTaskMap.remove(key);
            final Timer crtTimer = timerMap.remove(key);
            closeTimer(crtTimer);
        }
    }

    private void closeTimer(Timer timer) {
        try {
            timer.cancel();
            timer.purge();
        } catch (Exception ignored) {
        }
    }
}