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
package io.cloudslang.engine.data;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: froelica
 * Date: 4/29/13
 * Time: 10:05 AM
 *
 * This service is responsible for generating ids that are unique in the DB level.
 *
 */
public interface IdentityGenerator {

    /**
     * returns the next id available.
     *
     * @return a {@link java.lang.Long} of the next available id.
     */
    Long next();

    /**
     * return a bulk of a given size of ids
     *
     * @param bulkSize the amount of ids ro return
     * @return a {@link java.util.List<java.lang.Long>} of ids.
     */
    List<Long> bulk(int bulkSize);
}