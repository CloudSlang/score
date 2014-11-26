/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.engine.node.entities;

import org.eclipse.score.engine.data.AbstractIdentifiable;

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
