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
package io.cloudslang.orchestrator.services;

public interface AplsLicensingService {

    String BRANCH_ID_TO_CHECK_OUT_LICENSE = "BRANCH_ID_CHECKOUT";
    String BRANCH_ID_TO_CHECK_IN_LICENSE = "BRANCH_ID_CHECKIN";

    void checkoutBeginLane(String executionId, String branchId, long executionStartTimeMillis, int executionTimeoutMinutes);

    void checkinEndLane(String executionId, String branchId);

    boolean incrementUiStep(String executionId, String branchId);

    void decrementUiStep(String executionId, String branchId);
}
