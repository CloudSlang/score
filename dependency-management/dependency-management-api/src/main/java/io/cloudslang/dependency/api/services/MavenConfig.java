/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.dependency.api.services;

/**
 * @author Alexander Eskin
 */
public interface MavenConfig {
    String MAVEN_HOME = "maven.home";
    String MAVEN_REPO_LOCAL = "cloudslang.maven.repo.local";
    String MAVEN_REMOTE_URL = "cloudslang.maven.repo.remote.url";
    String MAVEN_PLUGINS_URL = "cloudslang.maven.plugins.remote.url";
    String USER_HOME = "user.home";

    String MAVEN_PROXY_PROTOCOL = "maven.proxy.protocol";
    String MAVEN_PROXY_HOST = "maven.proxy.host";
    String MAVEN_PROXY_PORT = "maven.proxy.port";
    String MAVEN_PROXY_NON_PROXY_HOSTS = "maven.proxy.non.proxy.hosts";

    String MAVEN_SETTINGS_PATH = "maven.settings.xml.path";
    String MAVEN_M2_CONF_PATH = "maven.m2.conf.path";


    char SEPARATOR = '/';
    String getLocalMavenRepoPath();
    String getRemoteMavenRepoUrl();
}
