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
package io.cloudslang.engine.versioning.services;

/**
 * Created with IntelliJ IDEA.
 * User: wahnonm
 * Date: 11/3/13
 * Time: 9:23 AM
 */
public interface VersionService {

    /**
     * The recovery key
     *
     */
    public static final String MSG_RECOVERY_VERSION_COUNTER_NAME = "MSG_RECOVERY_VERSION";

    /**
     * Given the counter name (key) returns the current version of it.
     * @param counterName : the counter name (key)
     * @return current count
     */
    public long getCurrentVersion(String counterName);

    /**
     * Increments the conuter relevent to the given counter name by 1.
     * @param counterName : the counter name (key) to increment.
     */
    public void incrementVersion(String counterName);
}
