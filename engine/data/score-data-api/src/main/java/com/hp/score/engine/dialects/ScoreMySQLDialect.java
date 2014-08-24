package com.hp.score.engine.dialects;

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