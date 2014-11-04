package com.hp.score.lang.compiler;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.lang.compiler.domain.SlangFile;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@SuppressWarnings("unchecked")
@Component
public class Compiler {

    @Autowired
    private List<Transformer> transformers;

    @Autowired
    private NamespaceBuilder namespaceBuilder;

    @Autowired
    private YamlParser yamlParser;

    public ExecutionPlan compile(File source, List<File> classpath) {

        SlangFile slangFile = yamlParser.loadMomaFile(source);

        //first we build a map of all the relevant files we got in the classpath sorted by their namespace
        TreeMap<String, File> namespaces = new TreeMap<>();
        if (classpath != null) {
            List<File> filterClassPath = namespaceBuilder.filterClassPath(classpath);
            namespaces = namespaceBuilder.sortByNameSpace(filterClassPath);
        }

//        then we filter the files that their namespace was not imported
        Map<String, ExecutionPlan> dependencies = new HashMap<>();
        if (slangFile.getImports() != null) {
            TreeMap<String, File> importsFiles = namespaceBuilder.filterNonImportedFiles(namespaces, slangFile.getImports());
            dependencies = compileDependencies(importsFiles, classpath);
            //todo cyclic dependencies
        }

        if (slangFile.getOperations() != null) {
            //todo the get(0).... is just for now.....!!!!>...
            return compileOperations(slangFile.getOperations(), dependencies).get(0);
        } else if (slangFile.getFlow() != null) {
            return compileFlow(slangFile.getFlow(), dependencies);
        } else {
            throw new RuntimeException("Nothing to compile");
        }
    }

    private TreeMap<String, ExecutionPlan> compileDependencies(TreeMap<String, File> dependencies, List<File> classpath) {
        TreeMap<String, ExecutionPlan> compiledDependencies = new TreeMap<>();
        for (Map.Entry<String, File> entry : dependencies.entrySet()) {
            //todo another hack...... for operation support.....
            if (entry.getValue() != null && !entry.getValue().getName().contains("operations")) {
                ExecutionPlan executionPlan = compile(entry.getValue(), classpath);
                compiledDependencies.put(entry.getKey(), executionPlan);
            }
        }

        return compiledDependencies;
    }

    private List<ExecutionPlan> compileOperations(List<Map> operationsRawData, Map<String, ExecutionPlan> dependencies) {
        List<ExecutionPlan> executionPlans = new ArrayList<>();
        for (Map operation : operationsRawData) {
            Map.Entry<String, Map> entry = (Map.Entry<String, Map>) operation.entrySet().iterator().next();
            Map<String, Object> operationRawData = entry.getValue();
            operationRawData.put("name", entry.getKey());
            executionPlans.add(compileOperation(operationRawData, dependencies));
        }
        return executionPlans;
    }

    private ExecutionPlan compileOperation(Map<String, Object> operationRawData, Map<String, ExecutionPlan> dependencies) {
        Map<String, Object> operationTransformedData = runTransformers(operationRawData, Scope.BEFORE_OPERATION, Scope.ACTION, Scope.AFTER_OPERATION);
        return null;
    }

    private Map<String, Object> runTransformers(Map<String, Object> rawData, Scope... relevantScopes) {
        Map<String, Object> transformedData = new HashMap<>();

        Iterator it = rawData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            boolean wasTransformed = false;
            for (Transformer transformer : transformers) {
                if (CollectionUtils.containsAny(transformer.getScopes(), Arrays.asList(relevantScopes))) {
                    String key = pairs.getKey().toString();
                    if (shouldApplyTransformer(transformer, key)) {
                        Object value = transformer.transform(rawData.get(key));
                        transformedData.put(key, value);
                        wasTransformed = true;
                    }
                }
            }
            it.remove();

            if (!wasTransformed) throw new RuntimeException("no transformer was found for: " + pairs.getKey());
        }
        return transformedData;
    }

    private ExecutionPlan compileFlow(Map<String, Object> flowRawData, Map<String, ExecutionPlan> dependencies) {
        return compileOperation(flowRawData, dependencies);
    }

    private boolean shouldApplyTransformer(Transformer transformer, String key) {
        String transformerName = transformer.getClass().getSimpleName().toLowerCase();
        return transformerName.startsWith(key.toLowerCase());
    }

}
