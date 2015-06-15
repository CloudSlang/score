/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.data;

import org.springframework.beans.factory.FactoryBean;

public class GeneratorFactoryBean implements FactoryBean<IdentityGenerator> {

    private IdentityGenerator identityGenerator;

    @Override
    public IdentityGenerator getObject() throws Exception {
        if (identityGenerator == null) {
            identityGenerator = new LocalMemIncrementGenerator();
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
