package com.hp.score.engine.data;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Component
public class HiloFactoryBean implements FactoryBean<IdentityGenerator> {

	@Autowired
    private DataSource dataSource;

    private IdentityGenerator<Long> identityGenerator;

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
