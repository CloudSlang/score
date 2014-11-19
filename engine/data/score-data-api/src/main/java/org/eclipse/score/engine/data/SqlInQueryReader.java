/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.engine.data;

import org.apache.commons.lang.Validate;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * User: maromg
 * Date: 18/07/13
 *
 * This class is used for executing queries that contain In clauses.
 * It is needed because databases have a limitation of 1000 (Depending on the database) items per in clause.
 */
public class SqlInQueryReader {

    private final int DATABASE_IN_CLAUSE_LIMIT = 1000;

    public <T> List<T> read(Set<String> items, SqlInQueryCallback<T> callback) {
        Validate.notNull(callback);
        if (items == null) {
            return Collections.emptyList();
        } else {
            return readItems(items, callback);
        }
    }

    private <T> List<T> readItems(Set<String> items, SqlInQueryCallback<T> callback) {
        Set<String> source = new HashSet<>(items);
        List<T> results = new LinkedList<>();
        do {
            Set<String> itemsToRead = extractAndRemoveUpToLimit(source, DATABASE_IN_CLAUSE_LIMIT);
            List<T> result = callback.readItems(itemsToRead);
            if(result!=null){
                results.addAll(result);
            }
        } while (!CollectionUtils.isEmpty(source));
        return results;
    }

    //Extracts items from source in the ammount set in 'limit' and also removes them from source
    private Set<String> extractAndRemoveUpToLimit(Set<String> source, int limit) {
        Set<String> result = new HashSet<>();
        Iterator<String> iterator = source.iterator();
        while (iterator.hasNext() && result.size() < limit) {
            result.add(iterator.next());
            iterator.remove();
        }
        return result;
    }

    public interface SqlInQueryCallback<T> {
        List<T> readItems(Set<String> items);
    }

}
