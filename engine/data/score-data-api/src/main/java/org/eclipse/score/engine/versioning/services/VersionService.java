/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.engine.versioning.services;

/**
 * Created with IntelliJ IDEA.
 * User: wahnonm
 * Date: 11/3/13
 * Time: 9:23 AM
 */
//TODO: Add Javadoc Meir
public interface VersionService {

    public static final String MSG_RECOVERY_VERSION_COUNTER_NAME = "MSG_RECOVERY_VERSION";

    public long getCurrentVersion(String counterName);


    public void incrementVersion(String counterName);
}
