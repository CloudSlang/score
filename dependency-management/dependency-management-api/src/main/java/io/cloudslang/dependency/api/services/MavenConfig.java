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

    String APP_HOME = "app.home";
    String LOGS_FOLDER_NAME = "logs";
    String MAVEN_FOLDER = "maven";
    String POM_EXTENSION = "pom";
    String LOG_FILE_FLAG = "--log-file";
    String DEPENDENCY_BUILD_CLASSPATH_COMMAND = "org.apache.maven.plugins:maven-dependency-plugin:3.6.0:build-classpath";
    String DEPENDENCY_GET_COMMAND = "org.apache.maven.plugins:maven-dependency-plugin:3.6.0:get";
    String MAVEN_SETTINGS_FILE_FLAG = "-s";
    String MAVEN_POM_PATH_PROPERTY = "-f";
    String MAVEN_CLASSWORLDS_CONF_PROPERTY = "classworlds.conf";
    String MAVEN_ARTIFACT_PROPERTY = "artifact";
    String MAVEN_MDEP_OUTPUT_FILE_PROPEPRTY = "mdep.outputFile";
    String MAVEN_MDEP_PATH_SEPARATOR_PROPERTY = "mdep.pathSeparator";
    String TRANSITIVE_PROPERTY = "transitive";

    String getLocalMavenRepoPath();
    String getRemoteMavenRepoUrl();
}
