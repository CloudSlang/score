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
package com.hp.score.engine.dialects;

import org.hibernate.dialect.Dialect;
import org.hibernate.service.jdbc.dialect.internal.StandardDialectResolver;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 22/07/14
 * Time: 11:52
 */
public class ScoreDialectResolver  extends StandardDialectResolver {

    @Override
    protected Dialect resolveDialectInternal(DatabaseMetaData metaData) throws SQLException {
        String databaseName = metaData.getDatabaseProductName();
        int databaseMajorVersion = metaData.getDatabaseMajorVersion();
        System.out.println("Database name is: " + databaseName + " databaseMajorVersion is: " + databaseMajorVersion);
        if ( "MySQL".equals( databaseName ) ) {
			return new ScoreMySQLDialect();
        }
        return super.resolveDialectInternal(metaData);
    }
}
