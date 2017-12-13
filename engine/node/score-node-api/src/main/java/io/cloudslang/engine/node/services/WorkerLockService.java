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

package io.cloudslang.engine.node.services;

/**
 * User: varelasa
 * Date: 20/07/14
 * Time: 11:25
 *
 * A service responsible for synchronizing between drain and recovery mechanisms
 */

public interface WorkerLockService {

    /**
     * Create the Worker Lock entry with the current worker uuid
     * @param uuid worker's unique identifier
     */
    void create(String uuid);

    /**
     * Delete the Worker Lock entry with the current worker uuid
     * @param uuid worker's unique identifier
     */
    void delete(String uuid);

    /**
     * Lock the Worker Lock entity with the current worker uuid
     * @param uuid worker's unique identifier
     */
    void lock(String uuid);
}
