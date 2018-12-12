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
package io.cloudslang.engine.queue.services;


import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.sql.Statement;

public class StatementAwareJdbcTemplateWrapper extends JdbcTemplate {
    private static final Logger log = Logger.getLogger(StatementAwareJdbcTemplateWrapper.class);

    private final String name;
    private final ThreadLocal<Integer> statementBatchSizeThreadLocal;

    public StatementAwareJdbcTemplateWrapper(DataSource dataSource, String name) {
        super(dataSource);
        this.name = name;
        this.statementBatchSizeThreadLocal = new ThreadLocal<>();
    }

    @Override
    protected void applyStatementSettings(Statement stmt) throws SQLException {
        Integer batchSize = this.statementBatchSizeThreadLocal.get();
        int batchSizeValue = (batchSize != null) ? batchSize : -1;
        if (batchSizeValue != -1) {
            stmt.setMaxRows(batchSizeValue);
            stmt.setFetchSize(batchSizeValue);
            if (log.isDebugEnabled()) {
                log.debug("For name " + name + " identified  batch size " + batchSizeValue + " for statement '" + stmt.toString() + "'");
            }
        }
        DataSourceUtils.applyTimeout(stmt, this.getDataSource(), this.getQueryTimeout());
    }

    public void setStatementBatchSize(int batchSize) {
        this.statementBatchSizeThreadLocal.set(batchSize);
    }

    public void clearStatementBatchSize() {
        this.statementBatchSizeThreadLocal.remove();
    }

}
