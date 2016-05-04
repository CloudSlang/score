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

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.cloudslang.dependency.api.services.MavenConfig.SEPARATOR;

/**
 * @author Alexander Eskin
 */
@Service
@SuppressWarnings("unused")
public class DependencyServiceImpl implements DependencyService {
    protected static final String DEPENDENCY_DELIMITER = ";";

    @Autowired
    private MavenConfig mavenConfig;

    @Override
    public Set<String> getDependencies(Set<String> resources) {
        Set<String> resolvedResources = new HashSet<>(resources.size());
        for (String resource : resources) {
            String[] gav = extractGav(resource);
            List<String> dependencyList = getDependencyList(gav);
            resolvedResources.addAll(dependencyList);
        }
        return resolvedResources;
    }

    private List<String> getDependencyList(String[] gav) {
        String dependencyFilePath = getResourceFolderPath(gav) + SEPARATOR + getDependencyFileName(gav);
        File file = new File(dependencyFilePath);
        if(!file.exists()) {
            //TODO load using maven
            throw new IllegalStateException(dependencyFilePath + " not found");
        }
        try {
            return parse(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getDependencyFileName(String[] gav) {
        return getArtifactID(gav) + '-' + getVersion(gav) + ".path";
    }

    private List<String> parse(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line = reader.readLine();
            String[] paths = line.split(DEPENDENCY_DELIMITER);
            return Arrays.asList(paths);
        }
    }

    private String getResourceFolderPath(String[] gav) {
        return mavenConfig.getLocalMavenRepoPath() + SEPARATOR +
                getGroupIDPath(gav) + SEPARATOR + getArtifactID(gav) + SEPARATOR + getVersion(gav);
    }

    private String[] extractGav(String resource) {
        String[] gav = resource.split(":");
        if(gav.length != 3) {
            throw new IllegalArgumentException("Unexpected resource format: " + resource +
                    ", should be <group ID>:<artifact ID>:<version>");
        }
        return gav;
    }

    private String getGroupIDPath(String[] gav) {
        return gav[0].replace('.', File.separatorChar);
    }

    private String getArtifactID(String[] gav) {
        return gav[1];
    }

    private String getVersion(String[] gav) {
        return gav[2];
    }
}
