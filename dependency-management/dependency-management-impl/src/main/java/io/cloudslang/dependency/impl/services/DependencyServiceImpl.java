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
import org.codehaus.plexus.classworlds.launcher.Launcher;
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
    public static final String M2_CONF = "m2.conf";
    public static final String SETTINGS_XML = "settings.xml";

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
            file = buildDependencyFile(gav);
        }
        try {
            return parse(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private File buildDependencyFile(String[] gav) {
        String pomFilePath = getResourceFolderPath(gav) + File.separator + getFileName(gav, "pom");
        System.setProperty("mdep.outputFile", getDependencyFileName(gav));
        System.setProperty("mdep.pathSeparator", DEPENDENCY_DELIMITER);
        System.setProperty("classworlds.conf", getClass().getClassLoader().getResource(M2_CONF).getPath());
        String[] args = new String[]{
                "-s",
                getClass().getClassLoader().getResource(SETTINGS_XML).getPath(),
                "-f",
                pomFilePath,
                "dependency:build-classpath"
        };
        try {
            int exitCode = Launcher.mainWithExitCode(args);
            if (exitCode != 0) {
                throw new RuntimeException("mvn dependency:build-classpath returned " +
                        exitCode + ", see log for details");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build classpath using Maven", e);
        }
        File fileToReturn = new File(getResourceFolderPath(gav) + File.separator + getDependencyFileName(gav));
        if(!fileToReturn.exists()) {
            throw new IllegalStateException(fileToReturn.getPath() + " not found");
        }
        appendSelfToPathFile(gav, fileToReturn);
        return fileToReturn;
    }

    private void appendSelfToPathFile(String[] gav, File pathFile) {
        File resourceFolder = new File(getResourceFolderPath(gav));
        if(!resourceFolder.exists() || !resourceFolder.isDirectory()) {
            throw new IllegalStateException("Directory " + resourceFolder.getPath() + " not found");
        }
        File[] files = resourceFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".zip");
            }
        });
        //we suppose that there should be either 1 jar or 1 zip
        if(files.length == 0) {
            throw new IllegalStateException("No resource is found in " + resourceFolder.getPath());
        }
        File resourceFile = files[0];
        try (FileWriter fw = new FileWriter(pathFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)){
            out.print(DEPENDENCY_DELIMITER);
            out.print(resourceFile.getCanonicalPath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append to file " + pathFile.getParent(), e);
        }
    }

    private String getResourceString(String[] gav) {
        return String.join(DEPENDENCY_DELIMITER, gav);
    }

    private String getDependencyFileName(String[] gav) {
        return getFileName(gav, "path");
    }

    private String getFileName(String[] gav, String extension) {
        return getArtifactID(gav) + '-' + getVersion(gav) + "." + extension;
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
