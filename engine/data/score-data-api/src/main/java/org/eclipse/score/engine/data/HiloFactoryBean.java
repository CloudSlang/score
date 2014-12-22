/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.engine.data;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

public class HiloFactoryBean implements FactoryBean<IdentityGenerator> {

	@Autowired
    private DataSource dataSource;

    private IdentityGenerator identityGenerator;

    @PostConstruct
    private void setupGenerator() {
        SimpleHiloIdentifierGenerator.setDataSource(dataSource);
    }

    @Override
    public IdentityGenerator getObject() throws Exception {
        if (identityGenerator == null) {
            identityGenerator = new SimpleHiloIdentifierGenerator();
        }
        return identityGenerator;
    }

    @Override
    public Class<?> getObjectType() {
        return IdentityGenerator.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
