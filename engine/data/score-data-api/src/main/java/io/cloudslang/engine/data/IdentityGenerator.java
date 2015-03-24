/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

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