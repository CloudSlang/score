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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

public class HiloFactoryBean implements FactoryBean<IdentityGenerator> {

	@Autowired
    @Qualifier("coreDataSource")
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
