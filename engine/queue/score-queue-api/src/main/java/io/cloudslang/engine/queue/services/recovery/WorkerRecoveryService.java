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

package io.cloudslang.engine.queue.services.recovery;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 8/6/14
 * Time: 9:12 AM
 */
public interface WorkerRecoveryService {

    /**
     * Used in order to recover non responsive worker
     * Also used during worker startup - in order to recover all data that was in worker before restart
     * @param workerUuid - the uuid of worker
     */
    void doWorkerRecovery(String workerUuid);

    /**
     * Used by the recovery job
     * Recovery will be done if the worker is non responsive or has not acknowledged messages
     * @param workerUuid - the uuid of worker
     */
    void doWorkerAndMessageRecovery(String workerUuid);
}
