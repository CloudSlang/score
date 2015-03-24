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

import org.hibernate.dialect.MySQLDialect;

import java.sql.Types;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 22/07/14
 * Time: 11:54
 */
public class ScoreMySQLDialect extends MySQLDialect {

    public ScoreMySQLDialect() {
        super();
        registerColumnType(Types.BOOLEAN, "bit");
    }
}