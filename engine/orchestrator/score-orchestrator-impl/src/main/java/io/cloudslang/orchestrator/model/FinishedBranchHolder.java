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
package io.cloudslang.orchestrator.model;

import io.cloudslang.orchestrator.entities.FinishedBranch;

import java.util.Objects;

public class FinishedBranchHolder {
    private final FinishedBranch finishedBranch;

    public FinishedBranchHolder(FinishedBranch finishedBranch) {
        this.finishedBranch = finishedBranch;
    }

    public FinishedBranch getFinishedBranch() {
        return finishedBranch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FinishedBranchHolder that = (FinishedBranchHolder) o;
        return Objects.equals(finishedBranch.getSplitId(), that.finishedBranch.getSplitId()) &&
                Objects.equals(finishedBranch.getBranchId(), that.finishedBranch.getBranchId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(finishedBranch.getSplitId(), finishedBranch.getBranchId());
    }
}
