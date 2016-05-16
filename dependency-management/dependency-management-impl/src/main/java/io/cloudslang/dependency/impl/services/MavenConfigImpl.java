/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.dependency.impl.services;

import io.cloudslang.dependency.api.services.MavenConfig;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author Alexander Eskin
 */
@SuppressWarnings("unused")
@Component
public class MavenConfigImpl implements MavenConfig {
    @Override
    public String getLocalMavenRepoPath() {
        String defValue = System.getProperty(USER_HOME) + SEPARATOR + ".m2" + SEPARATOR + "repository";
        return System.getProperty(MAVEN_REPO_LOCAL, defValue);
    }

    @Override
    public String getRemoteMavenRepoUrl() {
        //TODO
        return null;
    }
}
