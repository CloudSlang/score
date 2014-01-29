package com.hp.score.engine.data;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: froelica
 * Date: 12/11/13
 * Time: 3:10 PM
 */

public class DataBaseDetector {

	private final Logger logger = Logger.getLogger(getClass());

	private final static String MSSQL_PRODUCT_NAME = "Microsoft SQL Server";
	private final static String ORACLE_PRODUCT_NAME = "Oracle";

	@Autowired
	private DataSource dataSource;

	public boolean isMssql() {
		return isDataBaseMatch(MSSQL_PRODUCT_NAME);
	}

	public boolean isOracle() {
		return isDataBaseMatch(ORACLE_PRODUCT_NAME);
	}

	private boolean isDataBaseMatch(String databaseName){
		boolean match = false;
		try (Connection conn = dataSource.getConnection()) {
			if (conn != null) {
				match = conn.getMetaData().getDatabaseProductName().equals(databaseName);
			}
		} catch (SQLException e) {
			logger.error("Couldn't get database connection!", e);
			throw new RuntimeException("Couldn't get database connection!", e);
		}
		return match;
	}

}
