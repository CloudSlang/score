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
package io.cloudslang.engine.versioning.entities;

import io.cloudslang.engine.data.AbstractIdentifiable;
import org.apache.commons.lang.builder.EqualsBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;


/**
 * User: wahnonm
 * Date: 30/10/13
 * Time: 11:49
 */
@Entity
@Table(name = "OO_VERSION_COUNTERS")
public class VersionCounter extends AbstractIdentifiable {

    @Column(name = "COUNTER_NAME", nullable = false, unique = true, length = 64)
    private String counterName;

    @Column(name = "COUNTER_VERSION", nullable = false)
    private long versionCount;

    private VersionCounter(){};

    public VersionCounter(String counterName) {
        this.counterName = counterName;
    }

    public String getCounterName() {
        return counterName;
    }

    public long getVersionCount() {
        return versionCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionCounter that = (VersionCounter) o;
        return new EqualsBuilder()
                .append(this.counterName, that.counterName)
                .append(this.versionCount, that.versionCount)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(counterName,versionCount);
    }
}
