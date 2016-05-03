package io.cloudslang.dependency.impl.services;

/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

import io.cloudslang.dependency.api.services.DependencyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Service
public class DependencyServiceImpl implements DependencyService {

    @Value("#{systemProperties['maven.repo.local'] != null ? systemProperties['maven.repo.local'] : systemProperties['user.home'] + systemProperties['file.separator'] + '.m2' + systemProperties['file.separator'] + 'repository'}")
    private String mavenLocalRepo;

    @Override
    public Set<String> getDependencies(Set<String> resources) {
        Set<String> resolvedResources = new HashSet<>(resources.size());
        for (String resource : resources) {
            String [] gav = resource.split(":");
            String resourceFolderRelativePath = resource.replace(":", File.separator);
            String resouceFileName = gav[1] + "-" + gav[2] + ".jar";
            resolvedResources.add(mavenLocalRepo + File.separator + resourceFolderRelativePath + File.separator + resouceFileName);
        }
        return resolvedResources;
    }
}
