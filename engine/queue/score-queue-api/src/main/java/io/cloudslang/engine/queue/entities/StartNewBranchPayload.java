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
package io.cloudslang.engine.queue.entities;

import java.util.Objects;

public class StartNewBranchPayload {

    private long pendingExecutionStateId;
    private long pendingExecutionMapingId;

    public StartNewBranchPayload(long pendingExecutionStateId, long pendingExecutionMapingId) {
        this.pendingExecutionStateId = pendingExecutionStateId;
        this.pendingExecutionMapingId = pendingExecutionMapingId;
    }

    public long getPendingExecutionStateId() {
        return pendingExecutionStateId;
    }

    public void setPendingExecutionStateId(long pendingExecutionStateId) {
        this.pendingExecutionStateId = pendingExecutionStateId;
    }

    public long getPendingExecutionMapingId() {
        return pendingExecutionMapingId;
    }

    public void setPendingExecutionMapingId(long pendingExecutionMapingId) {
        this.pendingExecutionMapingId = pendingExecutionMapingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StartNewBranchPayload that = (StartNewBranchPayload) o;
        return pendingExecutionStateId == that.pendingExecutionStateId &&
                pendingExecutionMapingId == that.pendingExecutionMapingId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pendingExecutionStateId, pendingExecutionMapingId);
    }
}
