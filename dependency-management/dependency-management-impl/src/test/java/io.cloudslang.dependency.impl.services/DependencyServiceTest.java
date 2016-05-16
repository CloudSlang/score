package io.cloudslang.dependency.impl.services;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.utils.UnzipUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by eskin on 03/05/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DependencyServiceTest.TestConfig.class)
public class DependencyServiceTest {
    static  {
        ClassLoader classLoader = DependencyServiceTest.class.getClassLoader();
        String settingsXmlPath = classLoader.getResource("settings.xml").getPath();
        File rootHome = new File(settingsXmlPath).getParentFile();
        File mavenHome = new File(rootHome, "maven");
        UnzipUtil.unzipToFolder(mavenHome.getAbsolutePath(), classLoader.getResourceAsStream("maven.zip"));

        System.setProperty(MavenConfigImpl.MAVEN_HOME,  mavenHome.getAbsolutePath());

        System.setProperty(MavenConfigImpl.MAVEN_REPO_LOCAL, new TestConfig().mavenConfig().getLocalMavenRepoPath());
        System.setProperty(MavenConfigImpl.MAVEN_REMOTE_URL, "http://mydtbld0034.hpeswlab.net:8081/nexus/content/groups/oo-public");
        System.setProperty(MavenConfigImpl.MAVEN_PLUGINS_URL, "http://mydphdb0166.hpswlabs.adapps.hp.com:8081/nexus/content/repositories/snapshots/");
        System.setProperty("maven.home", classLoader.getResource("maven").getPath());

        System.setProperty(MavenConfigImpl.MAVEN_PROXY_PROTOCOL, "https");
        System.setProperty(MavenConfigImpl.MAVEN_PROXY_HOST, "proxy.bbn.hp.com");
        System.setProperty(MavenConfigImpl.MAVEN_PROXY_PORT, "8080");
        System.setProperty(MavenConfigImpl.MAVEN_PROXY_NON_PROXY_HOSTS, "*.hp.com");

        System.setProperty(MavenConfigImpl.MAVEN_SETTINGS_PATH, settingsXmlPath);
        System.setProperty(MavenConfigImpl.MAVEN_M2_CONF_PATH, classLoader.getResource("m2.conf").getPath());
    }


    @Autowired
    private DependencyService dependencyService;

    @After
    public void cleanup() {
        String basePath = new TestConfig().mavenConfig().getLocalMavenRepoPath();
        new File(basePath + "/junit/junit/4.12/junit-4.12.path").delete();
        new File(basePath + "/groupId1/mvn_artifact1/1.0/mvn_artifact1-1.0.path").delete();
    }

    @Test
    public void testMultipleDependencyResolution() {
        Set<String> ret = dependencyService.getDependencies(new HashSet<>(Arrays.asList("groupId1:test-artifact:1.0",
                "groupId1:test-artifact1:1.1")));
        List<String> referenceList = Arrays.asList("C:/aaa/bbb/ccc.jar", "C:/bbb/ccc/ddd.zip", "C:/ccc/ddd/eee/fff.jar",
                "C:/aaaa/bbbb/cccc.jar", "C:/bbbb/cccc/dddd.zip");
        Assert.assertTrue("Unexpected returned set", ret.containsAll(referenceList) && ret.size() == referenceList.size());
    }

    @Test
    public void testSingleDependencyResolution() {
        Set<String> ret = dependencyService.getDependencies(new HashSet<>(Collections.singletonList("groupId1:test-artifact1:1.1")));
        List<String> referenceList = Arrays.asList("C:/aaaa/bbbb/cccc.jar", "C:/bbbb/cccc/dddd.zip");
        Assert.assertTrue("Unexpected returned set", ret.containsAll(referenceList) && ret.size() == referenceList.size());
    }

    @Test
    public void testEmptyResourceSet() {
        Set<String> ret1 = dependencyService.getDependencies(new HashSet<String>());
        Assert.assertTrue("Unexpected returned set", ret1.isEmpty());
    }

    @Test
    public void testMalformedGav() {
        try {
            dependencyService.getDependencies(new HashSet<>(Collections.singletonList("groupId1:test-artifact1")));
            Assert.fail("Expected IllegalArgumentException, but succeeded");
        } catch (IllegalArgumentException ignore) {

        }
    }

    @Test public void testBuildClassPath1() {
        Set <String> ret = dependencyService.getDependencies(new HashSet<>(Collections.singletonList("groupId1:mvn_artifact1:1.0")));
        final List<File> retFiles = new ArrayList<>();
        ret.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                retFiles.add(new File(s));
            }
        });
        String basePath = new TestConfig().mavenConfig().getLocalMavenRepoPath();
        List<File> referenceList = Arrays.asList(
                new File(basePath + "/junit/junit/4.12/junit-4.12.jar"),
                new File(basePath + "/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"),
                new File(basePath + "/org/springframework/spring-core/4.2.5.RELEASE/spring-core-4.2.5.RELEASE.jar"),
                new File(basePath + "/commons-logging/commons-logging/1.2/commons-logging-1.2.jar"),
                new File(basePath + "/groupId1/mvn_artifact1/1.0/mvn_artifact1-1.0.jar"));
        Assert.assertTrue("Unexpected returned set", retFiles.containsAll(referenceList) && ret.size() == referenceList.size());
    }

    @Test public void testBuildClassPath2() {
        Set <String> ret = dependencyService.getDependencies(new HashSet<>(Collections.singletonList("junit:junit:4.12")));
        final List<File> retFiles = new ArrayList<>();
        ret.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                retFiles.add(new File(s));
            }
        });
        String basePath = new TestConfig().mavenConfig().getLocalMavenRepoPath();
        List<File> referenceList = Arrays.asList(
                new File(basePath + "/junit/junit/4.12/junit-4.12.jar"),
                new File(basePath + "/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"));
        Assert.assertTrue("Unexpected returned set", retFiles.containsAll(referenceList) && ret.size() == referenceList.size());
    }


    @Configuration
    static class TestConfig {
        @Bean
        public DependencyService dependencyService() {
            return new DependencyServiceImpl();
        }

        @Bean
        public MavenConfig mavenConfig() {
            return new MavenConfig() {
                @Override
                public String getLocalMavenRepoPath() {
                    String testMvnRepo = "test-mvn-repo";
                    URL url = getClass().getClassLoader().getResource(testMvnRepo);
                    if(url != null) {
                        return url.getPath();
                    }
                    return null;
                }

                @Override
                public String getRemoteMavenRepoUrl() {
                    return null;
                }
            };
        }

    }

}
