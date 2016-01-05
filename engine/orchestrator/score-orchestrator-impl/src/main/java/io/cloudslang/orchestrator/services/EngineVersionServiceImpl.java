package io.cloudslang.orchestrator.services;

import org.springframework.transaction.annotation.Transactional;

/**
 * Created by kravtsov on 03/01/2016
 */

public class EngineVersionServiceImpl implements EngineVersionService {
    @Override
    @Transactional
    public String getEngineVersionId() {
        return "";
    }
}
