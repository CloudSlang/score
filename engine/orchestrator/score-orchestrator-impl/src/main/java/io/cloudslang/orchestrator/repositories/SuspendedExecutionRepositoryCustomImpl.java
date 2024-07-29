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

package io.cloudslang.orchestrator.repositories;

import io.cloudslang.orchestrator.entities.SuspendedExecution;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnusedDeclaration")
public class SuspendedExecutionRepositoryCustomImpl implements SuspendedExecutionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SuspendedExecution> findBySplitIdIn(List<String> splitIds) {
        Query query = entityManager.createQuery("select se from SuspendedExecution se where " +
                "se.splitId in (" + getIdsAsString(splitIds) + ")");
        return query.getResultList();
    }

    @Override
    public int deleteByIds(Collection<String> ids) {
        Query query = entityManager.createQuery("delete from SuspendedExecution se where " +
                "se.executionId in (" + getIdsAsString(new ArrayList<>(ids)) + ")");
        return query.executeUpdate();
    }

    private String getIdsAsString(List<String> ids) {
        return ids.stream().map((executionStatus) -> "cast('" + executionStatus + "' as string)").collect(Collectors.joining(","));
    }
}
