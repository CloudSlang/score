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
package io.cloudslang.engine.queue.services.assigner.strategies;

import io.cloudslang.engine.queue.services.assigner.ChooseWorkerStrategy;

import java.util.concurrent.ConcurrentMap;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static io.cloudslang.engine.queue.enums.AssignStrategy.RANDOM;
import static io.cloudslang.engine.queue.enums.AssignStrategy.ROUND_ROBIN;
import static io.cloudslang.engine.queue.enums.AssignStrategy.getAssignedStrategy;
import static io.cloudslang.engine.queue.services.assigner.ExecutionAssignerServiceImpl.WORKER_MESSAGE_ASSIGNMENT_POLICY_KEY;
import static java.lang.Integer.getInteger;
import static java.util.concurrent.TimeUnit.HOURS;

public class RoundRobinStrategy implements ChooseWorkerStrategy {

    private static final int SEVEN_DAYS_IN_HOURS = 7 * 24;
    private static final int CACHE_MAX_ENTRIES = getInteger("worker.roundRobin.activeGroupAlias.maxSize", 10_000);
    private static final int MAX_CONCURRENCY_LEVEL = getInteger("worker.roundRobin.concurrencyLevel", 16);
    private static final int EXPIRE_NUMBER_OF_HOURS = getInteger("worker.roundRobin.expireAfterAccess", SEVEN_DAYS_IN_HOURS);

    private static final ConcurrentMap<String, Integer> groupAliasLatestWorkerMap = newBuilder()
            .maximumSize(isStrategyInUse() ? CACHE_MAX_ENTRIES : 0)
            .concurrencyLevel(MAX_CONCURRENCY_LEVEL)
            .expireAfterAccess(EXPIRE_NUMBER_OF_HOURS, HOURS)
            .<String, Integer>build().asMap();

    @Override
    public int getNextWorkerFromGroup(String groupAlias, int numberOfWorkersInGroup) {
        return groupAliasLatestWorkerMap.merge(groupAlias, 0,
                (oldValue, newValue) -> {
                    int nextOldValue = oldValue + 1;
                    return nextOldValue < numberOfWorkersInGroup ? nextOldValue : 0;
                });
    }

    private static boolean isStrategyInUse() {
        return getAssignedStrategy(System.getProperty(WORKER_MESSAGE_ASSIGNMENT_POLICY_KEY), RANDOM) == ROUND_ROBIN;
    }

}
