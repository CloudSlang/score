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
