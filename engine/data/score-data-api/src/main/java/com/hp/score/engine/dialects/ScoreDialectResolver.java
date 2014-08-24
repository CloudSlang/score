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
