package com.hp.score.lang.compiler;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.hp.score.lang.compiler.domain.SlangFile
;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class NamespaceBuilder {

    @Autowired
    private YamlParser yamlParser;

    public TreeMap<String, File> filterNonImportedFiles(TreeMap<String, File> namespaces, List<Map<String, String>> imports) {
        if (MapUtils.isEmpty(namespaces)) {
            throw new RuntimeException("no classpath but there are imports!!??");
        }
        TreeMap<String, File> importsFiles = new TreeMap<>();
        for (Map<String, String> anImport : imports) {
            Map.Entry<String, String> entry = anImport.entrySet().iterator().next();
            importsFiles.put(entry.getKey(), namespaces.get(entry.getValue().substring(0, entry.getValue().lastIndexOf("."))));
        }

        return importsFiles;
    }

    public List<File> filterClassPath(List<File> classpath) {
        String[] extensions = System.getProperty("classpath.extensions", "yaml,yml,py").split(",");
        List<File> filteredClassPath = new ArrayList<>();
        for (File file : classpath) {
            if (file.isDirectory()) {
                filteredClassPath.addAll(FileUtils.listFiles(file, extensions, true));
            } else {
                filteredClassPath.add(file);
            }
        }
        return filteredClassPath;
    }

    public TreeMap<String, File> sortByNameSpace(List<File> classpath) {
        TreeMap<String, File> namespaces = new TreeMap<>();
        final List<String> yamlExtensions = Arrays.asList("yaml", "yml");
        Predicate<File> isYaml = new Predicate<File>() {
            @Override
            public boolean apply(File file) {
                return yamlExtensions.contains(FilenameUtils.getExtension(file.getAbsolutePath()));
            }
        };
        Iterable<File> yamlFiles = Iterables.filter(classpath, isYaml);
//        Iterable<File> otherFiles = Iterables.filter(classpath, Predicates.not(isYaml));
        for (File yamlFile : yamlFiles) {
            SlangFile slangFile = yamlParser.loadMomaFile(yamlFile);
            namespaces.put(slangFile.getNamespace(), yamlFile);
        }
//        for (File file : otherFiles) {
//            namespaces.put(FilenameUtils.getBaseName(file.getAbsolutePath()), file);
//        }

        return namespaces;
    }


}
