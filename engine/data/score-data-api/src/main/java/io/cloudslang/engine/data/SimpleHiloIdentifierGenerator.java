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

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by IntelliJ IDEA.
 * User: froelica
 * Date: 4/25/13
 * Time: 11:03 AM
 */
public class SimpleHiloIdentifierGenerator implements IdentifierGenerator, IdentityGenerator {

    private final Logger logger = Logger.getLogger(getClass());

    static final String TABLE_NAME = "OO_HILO";
    static final String SQL_SELECT = "SELECT NEXT_HI FROM " + TABLE_NAME;
    static final String SQL_UPDATE = "UPDATE " + TABLE_NAME + " SET NEXT_HI = NEXT_HI+1";
    static final String SQL_LOCK = "UPDATE " + TABLE_NAME + " SET NEXT_HI = NEXT_HI";
    static final long CHUNK_SIZE = 100000L;

    private static DataSource dataSource;
    private int currentChunk;
    private long currentId;
    private Lock lock = new ReentrantLock();

    // been initialized by Hibernate
    public SimpleHiloIdentifierGenerator() {
        updateCurrentChunk();
    }

    public static void setDataSource(DataSource injectedDataSource) {
        dataSource = injectedDataSource;
    }

    @Override
    public Long next() {
        return (Long) generate(null, null);
    }

    @Override
    public List<Long> bulk(int bulkSize) {
        List<Long> idsList = new ArrayList<>();
        for (int i = 0; i < bulkSize; i++) {
            idsList.add(next());
        }
        return idsList;
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object)
            throws HibernateException {
        lock.lock();
        try {
            long id = ++currentId;
            if (id > CHUNK_SIZE) {
                if (logger.isDebugEnabled()) logger.debug("ID has reached chunk size");
                updateCurrentChunk();
                id = ++currentId;
            }
            return currentChunk * CHUNK_SIZE + id;
        } finally {
            lock.unlock();
        }
    }

    private void updateCurrentChunk() {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating HILO chunk...");
        }

        long t = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(conn, true));

            jdbcTemplate.update(SQL_LOCK);
            currentChunk = jdbcTemplate.queryForObject(SQL_SELECT, Integer.class);
            if (logger.isDebugEnabled())
                logger.debug("Current chunk: " + currentChunk);
            jdbcTemplate.execute(SQL_UPDATE);
            jdbcTemplate.execute("commit");

            if (logger.isDebugEnabled()) {
                logger.debug("Updating HILO chunk done in " + (System.currentTimeMillis() - t) + " ms");
            }
            currentId = 0;
        } catch (SQLException e) {
            logger.error("Unable to update current chunk", e);
            throw new IllegalStateException("Unable to update current chunk");
        }
    }
}
