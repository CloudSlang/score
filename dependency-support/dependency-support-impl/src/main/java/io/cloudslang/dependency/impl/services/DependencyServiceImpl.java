package io.cloudslang.dependency.impl.services;

import io.cloudslang.dependency.api.services.DependencyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class DependencyServiceImpl implements DependencyService {

    @Value("#{systemProperties['maven.repo.local'] != null ? systemProperties['maven.repo.local'] : systemProperties['user.home'] + systemProperties['file.separator'] + '.m2' + systemProperties['file.separator'] + 'repository'}")
    private String mavenLocalRepo;

    @Override
    public List<String> resolveDependencies(List<String> resources) {
        List<String> resolvedResources = new ArrayList<>(resources.size());
        for (String resource : resources) {
            String [] gav = resource.split(":");
            String resourceFolderRelativePath = resource.replace(":", File.separator);
            String resouceFileName = gav[1] + "-" + gav[2] + ".jar";
            resolvedResources.add(mavenLocalRepo + File.separator + resourceFolderRelativePath + File.separator + resouceFileName);
        }
        return resolvedResources;
    }
}
