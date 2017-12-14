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

package io.cloudslang.engine.node.entities;

import io.cloudslang.engine.data.AbstractIdentifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * User: varelasa
 * Date: 20/07/14
 * Time: 11:18
 */
@Entity
@Table(name = "OO_WORKER_LOCKS")
public class WorkerLock extends AbstractIdentifiable {

    @Column(name = "UUID", nullable = false, unique = true, length = 48)
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkerLock that = (WorkerLock) o;

        return uuid.equals(that.uuid);

    }

    @Override
    public int hashCode() {
        return  uuid.hashCode();
    }
}
