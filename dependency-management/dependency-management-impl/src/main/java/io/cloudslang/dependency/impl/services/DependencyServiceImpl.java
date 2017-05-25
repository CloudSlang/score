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
import io.cloudslang.score.events.ConfigurationAwareEventBus;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.cloudslang.dependency.api.services.MavenConfig.SEPARATOR;

/**
 * @author Alexander Eskin
 */
@Component
@SuppressWarnings("unused")
public class DependencyServiceImpl implements DependencyService {
    private static final Logger logger = Logger.getLogger(DependencyServiceImpl.class);

    private static final String MAVEN_LAUNCHER_CLASS_NAME = "org.codehaus.plexus.classworlds.launcher.Launcher";
    private static final String MAVEN_LANUCHER_METHOD_NAME = "mainWithExitCode";
    private static final String PATH_FILE_EXTENSION = "path";
    private static final String GAV_DELIMITER = ":";
    private static final String PATH_FILE_DELIMITER = ";";
    private static final int MINIMAL_GAV_PARTS = 3;
    private static final int MAXIMAL_GAV_PARTS = 5;
    private static final String GAV_SEPARATOR = "_";

    private Method launcherMethod;

    @Value("#{systemProperties['" + MavenConfig.MAVEN_HOME + "']}")
    private String mavenHome;

    private ClassLoader mavenClassLoader;

    private Method MAVEN_EXECUTE_METHOD;

    private String mavenLogFolder;

    @Autowired
    private MavenConfig mavenConfig;

    @Autowired
    private ConfigurationAwareEventBus eventBus;

    private final Lock lock = new ReentrantLock();

    @PostConstruct
    private void initMaven() throws ClassNotFoundException, NoSuchMethodException, MalformedURLException {
        ClassLoader parentClassLoader = DependencyServiceImpl.class.getClassLoader();
        while (parentClassLoader.getParent() != null) {
            parentClassLoader = parentClassLoader.getParent();
        }

        if (isMavenConfigured()) {
            File libDir = new File(mavenHome, "boot");
            if (libDir.exists()) {
                URL[] mavenJarUrls = getUrls(libDir);

                mavenClassLoader = new URLClassLoader(mavenJarUrls, parentClassLoader);
                MAVEN_EXECUTE_METHOD = Class.forName(MAVEN_LAUNCHER_CLASS_NAME, true, mavenClassLoader).
                        getMethod(MAVEN_LANUCHER_METHOD_NAME, String[].class);
                initMavenLogs();
            }
        }
    }

    private void initMavenLogs() {
        File mavenLogFolderFile = new File(new File(calculateLogFolderPath()), MavenConfig.MAVEN_FOLDER);
        boolean dirsCreated = mavenLogFolderFile.mkdirs();
        if (!dirsCreated) {
            logger.error("Failed to create maven log directories " + mavenLogFolderFile.getAbsolutePath());
        }
        this.mavenLogFolder = mavenLogFolderFile.getAbsolutePath();
    }

    private String calculateLogFolderPath() {
        Enumeration e = Logger.getRootLogger().getAllAppenders();
        while (e.hasMoreElements()) {
            Appender app = (Appender) e.nextElement();
            if (app instanceof FileAppender) {
                String logFile = ((FileAppender) app).getFile();
                return new File(logFile).getParentFile().getAbsolutePath();
            }
        }
        return new File(System.getProperty(MavenConfig.APP_HOME), MavenConfig.LOGS_FOLDER_NAME).getAbsolutePath();
    }

    protected PrintStream outputFile(String name) throws FileNotFoundException {
        File logFile = new File(name);
        File parentFile = logFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            logger.error("Failed to create parent folder [" + parentFile.getAbsolutePath() + "] for log file [" + name + "]");
        }
        return new PrintStream(new BufferedOutputStream(new FileOutputStream(name)));
    }

    private boolean isMavenConfigured() {
        return (mavenHome != null) && !mavenHome.isEmpty();
    }

    private URL[] getUrls(File libDir) throws MalformedURLException {
        File[] mavenJars = libDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return StringUtils.endsWithIgnoreCase(name, "jar");
            }
        });

        URL[] mavenJarUrls = new URL[mavenJars.length];
        for (int i = 0; i < mavenJarUrls.length; i++) {
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
                String dependencyFilePath = getResourceFolderPath(gav) + SEPARATOR + getPathFileName(gav);
                File file = new File(dependencyFilePath);
                if (!file.exists()) {
                    lock.lock();
                    try {
                        //double check if file was just created
                        if (!file.exists()) {
                            buildDependencyFile(gav);
                        }
                    } finally {
                        lock.unlock();
                    }
                }
                dependencyList = parse(file);
                resolvedResources.addAll(dependencyList);
            } catch (IOException|InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return resolvedResources;
    }

    @SuppressWarnings("ConstantConditions")
    private void buildDependencyFile(String[] gav) throws InterruptedException {
        sendMavenDependencyBuildEvent(gav);
        String pomFilePath = getPomFilePath(gav);
        downloadArtifacts(gav);
        System.setProperty(MavenConfig.MAVEN_MDEP_OUTPUT_FILE_PROPEPRTY, getPathFileName(gav));
        System.setProperty(MavenConfig.MAVEN_MDEP_PATH_SEPARATOR_PROPERTY, PATH_FILE_DELIMITER);
        System.setProperty(MavenConfig.MAVEN_CLASSWORLDS_CONF_PROPERTY, System.getProperty(MavenConfig.MAVEN_M2_CONF_PATH));
        String[] args = new String[]{
                MavenConfig.MAVEN_SETTINGS_FILE_FLAG,
                System.getProperty(MavenConfig.MAVEN_SETTINGS_PATH),
                MavenConfig.MAVEN_POM_PATH_PROPERTY,
                pomFilePath,
                MavenConfig.DEPENDENCY_BUILD_CLASSPATH_COMMAND,
                MavenConfig.LOG_FILE_FLAG,
                constructGavLogFilePath(gav, "build")
        };

        try {
            invokeMavenLauncher(args);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build classpath using Maven", e);
        }

        File fileToReturn = new File(getResourceFolderPath(gav) + SEPARATOR + getPathFileName(gav));
        if (!fileToReturn.exists()) {
            throw new IllegalStateException(fileToReturn.getPath() + " not found");
        }
        appendSelfToPathFile(gav, fileToReturn);
        sendMavenDependencyBuildFinishedEvent(gav);
    }

    private void sendMavenDependencyBuildFinishedEvent(String[] gav) throws InterruptedException {
        String message = String.format("Download complete for artifact with gav: %s ",
                StringUtils.arrayToDelimitedString(gav, ":"));

        Map<String, Serializable> data = new HashMap<>();
        data.put(EventConstants.MAVEN_DEPENDENCY_BUILD_FINISHED, message);
        dispatchEvent(new ScoreEvent(EventConstants.MAVEN_DEPENDENCY_BUILD_FINISHED, (Serializable) data));
    }

    private void sendMavenDependencyBuildEvent(String[] gav) throws InterruptedException {
        String message = String.format("Downloading artifact with gav: %s ",
                StringUtils.arrayToDelimitedString(gav, ":"));

        Map<String, Serializable> data = new HashMap<>();
        data.put(EventConstants.MAVEN_DEPENDENCY_BUILD, message);
        dispatchEvent(new ScoreEvent(EventConstants.MAVEN_DEPENDENCY_BUILD, (Serializable) data));
    }

    private void dispatchEvent(ScoreEvent eventWrapper) throws InterruptedException {
        eventBus.dispatch(eventWrapper);
    }

    private String getPomFilePath(String[] gav) {
        return getResourceFolderPath(gav) + SEPARATOR + getFileName(gav, MavenConfig.POM_EXTENSION);
    }

    private String constructGavLogFilePath(String[] gav, String what) {
        return new File(mavenLogFolder, gav[0] + GAV_SEPARATOR + gav[1] + GAV_SEPARATOR + gav[2] + GAV_SEPARATOR +
                what + ".log").getAbsolutePath();
    }

    private void invokeMavenLauncher(String[] args) throws Exception {
        ClassLoader origCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mavenClassLoader);
        try {
            int exitCode = (Integer) MAVEN_EXECUTE_METHOD.invoke(null, new Object[]{args});
            if (exitCode != 0) {
                throw new RuntimeException("mvn " + StringUtils.arrayToDelimitedString(args, " ") + " returned " +
                        exitCode + ", see log for details");
            }
        } finally {
            Thread.currentThread().setContextClassLoader(origCL);
        }
    }

    private void downloadArtifacts(String[] gav) {
        getDependencies(gav, false);
        getDependencies(gav, true);
    }

    private void getDependencies(String[] gav, Boolean transitive) {
        System.setProperty(MavenConfig.MAVEN_ARTIFACT_PROPERTY, getResourceString(gav, transitive));
        System.setProperty(MavenConfig.MAVEN_CLASSWORLDS_CONF_PROPERTY, System.getProperty(MavenConfig.MAVEN_M2_CONF_PATH));
        System.setProperty(MavenConfig.TRANSITIVE_PROPERTY, transitive.toString());
        String[] args = new String[]{
                MavenConfig.MAVEN_SETTINGS_FILE_FLAG,
                System.getProperty(MavenConfig.MAVEN_SETTINGS_PATH),
                MavenConfig.DEPENDENCY_GET_COMMAND,
                MavenConfig.LOG_FILE_FLAG,
                constructGavLogFilePath(gav, "get")
        };

        try {
            invokeMavenLauncher(args);
            if (!transitive) {
                removeTestScopeDependencies(gav);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to download resources using Maven", e);
        } finally {
            System.getProperties().remove(MavenConfig.TRANSITIVE_PROPERTY);
        }

    }

    private void removeTestScopeDependencies(String[] gav) {
        String pomFilePath = getPomFilePath(gav);
        try {
            removeByXpathExpression(pomFilePath, "/project/dependencies/dependency[scope[contains(text(), 'test')]]");
            removeByXpathExpression(pomFilePath, "/project/dependencyManagement/dependencies/dependency[scope[contains(text(), 'test')]]");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void removeByXpathExpression(String pomFilePath, String expression) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, TransformerException {
        File xmlFile = new File(pomFilePath);
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nl = (NodeList) xpath.compile(expression).
                evaluate(doc, XPathConstants.NODESET);

        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                node.getParentNode().removeChild(node);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            // need to convert to file and then to path to override a problem with spaces
            Result output = new StreamResult(new File(pomFilePath).getPath());
            Source input = new DOMSource(doc);
            transformer.transform(input, output);
        }
    }

    private void appendSelfToPathFile(String[] gav, File pathFile) {
        File resourceFolder = new File(getResourceFolderPath(gav));
        if (!resourceFolder.exists() || !resourceFolder.isDirectory()) {
            throw new IllegalStateException("Directory " + resourceFolder.getPath() + " not found");
        }
        File[] files = resourceFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".zip");
            }
        });
        //we suppose that there should be either 1 jar or 1 zip
        if (files.length == 0) {
            throw new IllegalStateException("No resource is found in " + resourceFolder.getPath());
        }
        File resourceFile = files[0];
        try (FileWriter fw = new FileWriter(pathFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(PATH_FILE_DELIMITER);
            out.print(resourceFile.getCanonicalPath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append to file " + pathFile.getParent(), e);
        }
    }

    private String getResourceString(String[] gav, boolean transitive) {
        //if not transitive, use type "pom"
        String[] newGav = new String[Math.max(4, gav.length)];
        System.arraycopy(gav, 0, newGav, 0, gav.length);
        if (!transitive) {
            newGav[3] = "pom";
        } else {
            if (newGav[3] == null) {
                newGav[3] = "jar";
            }
        }
        return StringUtils.arrayToDelimitedString(newGav, GAV_DELIMITER);
    }

    private String getPathFileName(String[] gav) {
        return getFileName(gav, PATH_FILE_EXTENSION);
    }

    private String getFileName(String[] gav, String extension) {
        return getArtifactID(gav) + '-' + getVersion(gav) + "." + extension;
    }

    private List<String> parse(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line = reader.readLine();
            if (line.startsWith(PATH_FILE_DELIMITER)) {
                line = line.substring(PATH_FILE_DELIMITER.length());
            }
            String[] paths = line.split(PATH_FILE_DELIMITER);
            return Arrays.asList(paths);
        }
    }

    private String getResourceFolderPath(String[] gav) {
        return mavenConfig.getLocalMavenRepoPath() + SEPARATOR +
                getGroupIDPath(gav) + SEPARATOR + getArtifactID(gav) + SEPARATOR + getVersion(gav);
    }

    private String[] extractGav(String resource) {
        String[] gav = resource.split(GAV_DELIMITER);
        if ((gav.length < MINIMAL_GAV_PARTS) || (gav.length > MAXIMAL_GAV_PARTS)) {//at least g:a:v at maximum g:a:v:p:c
            throw new IllegalArgumentException("Unexpected resource format: " + resource +
                    ", should be <group ID>:<artifact ID>:<version> or <group ID>:<artifact ID>:<version>:<packaging> or <group ID>:<artifact ID>:<version>:<packaging>:<classifier>");
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
}
