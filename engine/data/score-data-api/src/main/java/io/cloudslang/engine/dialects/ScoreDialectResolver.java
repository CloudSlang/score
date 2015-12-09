/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.dialects;

import org.apache.log4j.Logger;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 22/07/14
 * Time: 11:52
 */
public class ScoreDialectResolver  extends StandardDialectResolver {

    private final Logger logger = Logger.getLogger(getClass());

    protected Dialect resolveDialectInternal(DatabaseMetaData metaData) throws SQLException {
        String databaseName = metaData.getDatabaseProductName();
        int databaseMajorVersion = metaData.getDatabaseMajorVersion();

        logger.info("Database name is: " + databaseName + " databaseMajorVersion is: " + databaseMajorVersion);
        
        if ( "MySQL".equals( databaseName ) ) {
			return new ScoreMySQLDialect();
        }
        DatabaseMetaDataDialectResolutionInfoAdapter databaseMetaDataDialectResolutionInfoAdapter=new DatabaseMetaDataDialectResolutionInfoAdapter(metaData);
        return resolveDialect(databaseMetaDataDialectResolutionInfoAdapter);
    }
}
