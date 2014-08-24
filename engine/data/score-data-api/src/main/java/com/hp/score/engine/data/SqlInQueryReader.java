package com.hp.score.engine.data;

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
