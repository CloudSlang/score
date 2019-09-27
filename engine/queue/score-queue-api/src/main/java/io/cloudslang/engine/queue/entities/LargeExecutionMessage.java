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

import org.apache.commons.lang.builder.EqualsBuilder;

import java.io.Serializable;
import java.util.Objects;

public class LargeExecutionMessage implements Serializable {

    private static final long serialVersionUID = 3523623124812765965L;

    private long id;
    private long payloadSize;

    private int retriesCount;
    private long createTime;
    private long updateTime;

    public LargeExecutionMessage() {
        id = -1;
        payloadSize = 0;
        retriesCount = 0;
        updateTime = 0;
    }

    public LargeExecutionMessage(long id, long payloadSize, int retriesCount, long createTime, long updateTime) {
        this.id = id;
        this.payloadSize = payloadSize;
        this.retriesCount = retriesCount;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public long getId() {
        return id;
    }

    public long getPayloadSize() {
        return payloadSize;
    }

    public void setPayloadSize(long payloadSize) {
        this.payloadSize = payloadSize;
    }

    public int getRetriesCount() {
        return retriesCount;
    }

    public void setRetriesCount(int retriesCount) {
        this.retriesCount = retriesCount;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void incrementRetriesCounter() {
        this.retriesCount++;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LargeExecutionMessage that = (LargeExecutionMessage) o;
        return new EqualsBuilder()
                .append(this.id, that.id)
                .append(this.payloadSize, that.payloadSize)
                .append(this.retriesCount, that.retriesCount)
                .append(this.createTime, that.createTime)
                .append(this.updateTime, that.updateTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                payloadSize,
                retriesCount,
                createTime,
                updateTime
        );
    }
}
