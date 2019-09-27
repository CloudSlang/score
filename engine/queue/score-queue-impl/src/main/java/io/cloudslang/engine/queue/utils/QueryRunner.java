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
package io.cloudslang.engine.queue.utils;

import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public final class QueryRunner {

    private static final Logger logger = Logger.getLogger(QueryRunner.class);

    private QueryRunner() {
    }

    public static <T> List<T> doSelectWithTemplate(
            JdbcTemplate jdbcTemplate, String sql, RowMapper<T> rowMapper, Object... params) {
        logSQL(sql,params);
        try {
            long t = System.currentTimeMillis();
            List<T> result = jdbcTemplate.query(sql, params, rowMapper);
            if (logger.isDebugEnabled())
                logger.debug("Fetched result: " + result.size() + '/' + (System.currentTimeMillis() - t) + " rows/ms");
            return result;
        } catch (RuntimeException ex) {
            logger.error("Failed to execute query: " + sql, ex);
            throw ex;
        }
    }

    public static void logSQL(String query, Object... params) {
        if (logger.isDebugEnabled()) {
            logger.debug("Execute SQL: " + query);
            if (params != null && params.length > 1) logger.debug("Parameters : " + Arrays.toString(params));
        }
    }
}
