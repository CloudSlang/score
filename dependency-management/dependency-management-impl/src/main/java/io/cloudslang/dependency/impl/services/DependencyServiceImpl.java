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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.cloudslang.dependency.api.services.MavenConfig.SEPARATOR;

/**
 * @author Alexander Eskin
 */
@SuppressWarnings("unused")
public class DependencyServiceImpl implements DependencyService {
    private static final String MAVEN_LAUNCHER_CLASS_NAME = "org.codehaus.plexus.classworlds.launcher.Launcher";
    private static final String MAVEN_LANUCHER_METHOD_NAME = "mainWithExitCode";
    public static final String DEPENDENCY_FILE_EXTENSION = "path";

    private Method launcherMethod;

    @Value("#{systemProperties['" + MavenConfig.MAVEN_HOME + "']}")
    private String mavenHome;

    private ClassLoader mavenClassLoader;

    protected static final String DEPENDENCY_DELIMITER = ";";

    @Autowired
    private MavenConfig mavenConfig;

    Lock lock = new ReentrantLock();

    @PostConstruct
    private void initMaven() throws ClassNotFoundException, NoSuchMethodException, MalformedURLException {
        ClassLoader parentClassLoader = DependencyServiceImpl.class.getClassLoader();
        while(parentClassLoader.getParent() != null) {
            parentClassLoader = parentClassLoader.getParent();
        }

        File libDir = new File(mavenHome, "boot");
        URL[] mavenJarUrls = getUrls(libDir);

        mavenClassLoader = new URLClassLoader(mavenJarUrls, parentClassLoader);
    }

    private URL[] getUrls(File libDir) throws MalformedURLException {
        File[] mavenJars = libDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("jar");
            }
        });

        URL[] mavenJarUrls = new URL[mavenJars.length];
        for(int i = 0; i < mavenJarUrls.length; i++) {
            mavenJarUrls[i] = mavenJars[i].toURI().toURL();
        }
        return mavenJarUrls;
    }

    @Override
    public Set<String> getDependencies(Set<String> resources) {
        Set<String> resolvedResources = new HashSet<>(resources.size());
        for (String resource : resources) {
            String[] gav = extractGav(resource);
            List<String> dependencyList;
            try {
                String dependencyFilePath = getResourceFolderPath(gav) + SEPARATOR + getDependencyFileName(gav);
                File file = new File(dependencyFilePath);
                if(!file.exists()) {
                    lock.lock();
                    try {
                        buildDependencyFile(gav);
                    } finally {
                        lock.unlock();
                    }
                }
                dependencyList = parse(file);
                resolvedResources.addAll(dependencyList);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return resolvedResources;
    }

    @SuppressWarnings("ConstantConditions")
    private void buildDependencyFile(String[] gav) {
        String pomFilePath = getResourceFolderPath(gav) + SEPARATOR + getFileName(gav, "pom");
        downloadArtifactsIfNeeded(pomFilePath, gav);
        System.setProperty("mdep.outputFile", getDependencyFileName(gav));
        System.setProperty("mdep.pathSeparator", DEPENDENCY_DELIMITER);
        System.setProperty("classworlds.conf", System.getProperty(MavenConfig.MAVEN_M2_CONF_PATH));
        String[] args = new String[]{
                "-s",
                System.getProperty(MavenConfig.MAVEN_SETTINGS_PATH),
                "-f",
                pomFilePath,
                "dependency:build-classpath"
        };

        try {
            invokeMavenLauncher(args);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build classpath using Maven", e);
        }

        File fileToReturn = new File(getResourceFolderPath(gav) + SEPARATOR + getDependencyFileName(gav));
        if(!fileToReturn.exists()) {
            throw new IllegalStateException(fileToReturn.getPath() + " not found");
        }
        appendSelfToPathFile(gav, fileToReturn);
    }

    private void invokeMavenLauncher(String[] args) throws Exception {
        ClassLoader origCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mavenClassLoader);
        try {
            Object exitCodeObj = Class.forName(MAVEN_LAUNCHER_CLASS_NAME, true, mavenClassLoader).
                    getMethod(MAVEN_LANUCHER_METHOD_NAME, String[].class).invoke (null, new Object[]{args});
            int exitCode = (Integer)exitCodeObj;
            if (exitCode != 0) {
                throw new RuntimeException("mvn " + StringUtils.arrayToDelimitedString(args, " ") + " returned " +
                        exitCode + ", see log for details");
            }
        } finally {
            Thread.currentThread().setContextClassLoader(origCL);
        }
    }

    private void downloadArtifactsIfNeeded(String pomFilePath, String[] gav) {
        File pomFile = new File(pomFilePath);
        if(!pomFile.exists()) {
            downloadArtifacts(gav);
        }
    }

    private void downloadArtifacts(String[] gav) {
        System.setProperty("artifact", getResourceString(gav));
        System.setProperty("classworlds.conf", System.getProperty(MavenConfig.MAVEN_M2_CONF_PATH));
        String[] args = new String[]{
                "-s",
                System.getProperty(MavenConfig.MAVEN_SETTINGS_PATH),
                "dependency:get"
        };

        try {
            invokeMavenLauncher(args);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to download resources using Maven", e);
        }

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
        return StringUtils.arrayToDelimitedString(gav, ":");
    }

    private String getDependencyFileName(String[] gav) {
        return getFileName(gav, DEPENDENCY_FILE_EXTENSION);
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
        return gav[0].replace('.', SEPARATOR);
    }

    private String getArtifactID(String[] gav) {
        return gav[1];
    }

    private String getVersion(String[] gav) {
        return gav[2];
    }

    private void appendOutputFileProperty(String outputFilePath, Document doc, Node node) {
        Element outputFileNode = doc.createElement("mdep.outputFile");
        outputFileNode.setTextContent(outputFilePath);
        node.appendChild(outputFileNode);
    }
}
