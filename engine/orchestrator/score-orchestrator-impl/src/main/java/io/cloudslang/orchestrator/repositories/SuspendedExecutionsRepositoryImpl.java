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

import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

public class SuspendedExecutionsRepositoryImpl implements SuspendedExecutionCustomRepository {

    @PersistenceContext
    EntityManager entityManager;
    private final Logger log = Logger.getLogger(SuspendedExecutionsRepositoryImpl.class);
    @Override
    public List<String> listAllCompletedSuspendedExecution(int pageSize) {

        String queryString = "SELECT DISTINCT SE.EXECUTION_ID FROM OO_SUSPENDED_EXECUTIONS SE " +
                             " INNER JOIN OO_EXECUTION_SUMMARY ES ON SE.EXECUTION_ID = cast(ES.EXECUTION_ID as varchar(50))  " +
                             " LEFT JOIN OO_EXECUTION_STATE EST ON SE.EXECUTION_ID = cast(EST.EXECUTION_ID as varchar(50)) " +
                             " WHERE ES.END_TIME_LONG IS NOT NULL AND ES.BRANCH_ID = 'EMPTY' AND EST.EXECUTION_ID IS NULL";
        Query query = entityManager.createNativeQuery(queryString).setMaxResults(pageSize);
        return query.getResultList();
    }
}