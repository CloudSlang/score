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

import org.apache.http.conn.ClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.TimerTask;

import static java.lang.Integer.getInteger;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This class is responsible for Connection Eviction from the http connection pool
 * Taken from Apache documentation:
 * <p>
 * The stale connection check is not 100% reliable. The only feasible solution that does not involve a one thread per socket
 * model for idle connections is a dedicated monitor thread used to evict connections that are considered expired due
 * to a long period of inactivity. The monitor thread can periodically call ClientConnectionManager#closeExpiredConnections()
 * method to close all expired connections and evict closed connections from the pool.
 * It can also optionally call ClientConnectionManager#closeIdleConnections() method to close all connections
 * that have been idle over a given period of time.
 * <p>
 * Check https://hc.apache.org/httpcomponents-client-4.5.x/current/tutorial/html/connmgmt.html
 */
@SuppressWarnings("deprecation")
public class HttpPoolCleanupTask extends TimerTask implements Runnable {

    private static final Logger logger = LogManager.getLogger(HttpPoolCleanupTask.class);
    // We use 3.5 minutes for idle connection check period
    private static final int IDLE_CONNECTIONS_TIMEOUT_IN_SECONDS = getInteger("restClient.idleConnectionTimeoutSeconds", 210);

    private final ClientConnectionManager clientConnectionManager;

    public HttpPoolCleanupTask(ClientConnectionManager clientConnectionManager) {
        this.clientConnectionManager = requireNonNull(clientConnectionManager, "Connection manager cannot be null");
    }

    @Override
    public void run() {
        try {
            cleanup();
        } catch (Exception cleanupException) {
        }
    }

    private void cleanup() {
        try {
            clientConnectionManager.closeExpiredConnections();
        } catch (Exception closeExpiredConnectionsException) {
            logger.error("Could not close expired connections: ", closeExpiredConnectionsException);
        }

        try {
            clientConnectionManager.closeIdleConnections(IDLE_CONNECTIONS_TIMEOUT_IN_SECONDS, SECONDS);
        } catch (Exception closeIdleConnectionsException) {
            logger.error("Could not close idle connections: ", closeIdleConnectionsException);
        }
    }
}
