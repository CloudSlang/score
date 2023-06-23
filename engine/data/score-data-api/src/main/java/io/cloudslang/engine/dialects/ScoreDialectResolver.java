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
package io.cloudslang.engine.dialects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 22/07/14
 * Time: 11:52
 */
public class ScoreDialectResolver implements DialectResolver {

    private static final long serialVersionUID = 2544153575193017888L;

    @Autowired
    StandardDialectResolver dialectResolver;

    private final Logger logger = LogManager.getLogger(getClass());

    @Override
    public Dialect resolveDialect(DialectResolutionInfo metaData) {
        String databaseName = metaData.getDatabaseName();
        int databaseMajorVersion = metaData.getDatabaseMajorVersion();

        logger.info("Database name is: " + databaseName + " databaseMajorVersion is: " + databaseMajorVersion);

        if ("MySQL".equals(databaseName)) {
            return new ScoreMySQLDialect();
        } else if ("H2".equals(databaseName)) {
            return new ScoreH2Dialect();
        }
        return dialectResolver.resolveDialect(metaData);
    }
}
