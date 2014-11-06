package com.hp.score.lang.compiler;
/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

/*
 * Created by orius123 on 05/11/14.
 */

import ch.lambdaj.Lambda;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.lang.compiler.domain.DoAction;
import com.hp.score.lang.compiler.domain.Operation;
import com.hp.score.lang.compiler.domain.SlangFile;
import com.hp.score.lang.compiler.transformers.Transformer;
import com.hp.score.lang.compiler.utils.ExecutionStepFactory;
import com.hp.score.lang.compiler.utils.NamespaceBuilder;
import com.hp.score.lang.compiler.utils.YamlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;

@SuppressWarnings("unchecked")
@Component
public class SlangCompiler {

    private static final String SLANG_NAME = "slang";

    @Autowired
    private List<Transformer> transformers;

    @Autowired
    private NamespaceBuilder namespaceBuilder;

    @Autowired
    private YamlParser yamlParser;

    @Autowired
    private ExecutionStepFactory stepFactory;

    public ExecutionPlan compile(File source, List<File> classpath) {

        SlangFile slangFile = yamlParser.loadMomaFile(source);

        Map<String, ExecutionPlan> dependencies = handleDependencies(classpath, slangFile);

        if (slangFile.getOperations() != null) {
            List<ExecutionPlan> operationsExecutionPlans = compileOperations(slangFile.getOperations(), dependencies);
            //todo for now we get(0) (the first) operation always so we are able to compile one op, should be changed to get the op by name.
            ExecutionPlan executionPlan = operationsExecutionPlans.get(0);
            executionPlan.setFlowUuid(slangFile.getNamespace() + "." + executionPlan.getName());
            return executionPlan;
        } else if (slangFile.getFlow() != null) {
            return compileFlow(slangFile.getFlow(), dependencies);
        } else {
            throw new RuntimeException("Nothing to compile");
        }
    }

    private Map<String, ExecutionPlan> handleDependencies(List<File> classpath, SlangFile slangFile) {
//        first we build a map of all the relevant files we got in the classpath sorted by their namespace
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
        return dependencies;
    }

    private TreeMap<String, ExecutionPlan> compileDependencies(TreeMap<String, File> dependencies, List<File> classpath) {
        TreeMap<String, ExecutionPlan> compiledDependencies = new TreeMap<>();
        for (Map.Entry<String, File> entry : dependencies.entrySet()) {
            //todo another hack...... for operation support.....
            if (entry.getValue() != null && !entry.getValue().getName().contains(SlangTextualKeys.OPERATIONS_KEY)) {
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
            ExecutionPlan executionPlan = compileOperation(entry.getValue(), dependencies);
            executionPlan.setName(entry.getKey());
            executionPlans.add(executionPlan);
        }
        return executionPlans;
    }

    private ExecutionPlan compileOperation(Map<String, Object> operationRawData, Map<String, ExecutionPlan> dependencies) {
        Map<String, Serializable> preOperationActionData = new HashMap<>();
        Map<String, Serializable> postOperationActionData = new HashMap<>();
        DoAction doAction = null;

        Iterator it = operationRawData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            boolean wasTransformed = false;
            String key = pairs.getKey().toString();

            if (key.equals(SlangTextualKeys.ACTION_KEY)) {
                doAction = compileAction((Map<String, Object>) pairs.getValue());
                wasTransformed = true;
            }

            List<Transformer> preOpTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Scope.BEFORE_OPERATION)), transformers);
            for (Transformer transformer : preOpTransformers) {
                if (shouldApplyTransformer(transformer, key)) {
                    Object value = transformer.transform(operationRawData.get(key));
                    preOperationActionData.put(key, (Serializable) value);
                    wasTransformed = true;
                }
            }

            List<Transformer> postOpTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Scope.AFTER_OPERATION)), transformers);
            for (Transformer transformer : postOpTransformers) {
                if (shouldApplyTransformer(transformer, key)) {
                    Object value = transformer.transform(operationRawData.get(key));
                    postOperationActionData.put(key, (Serializable) value);
                    wasTransformed = true;
                }
            }

            it.remove();

            if (!wasTransformed) throw new RuntimeException("no transformer was found for: " + pairs.getKey());
        }

        Operation operation = new Operation(preOperationActionData, postOperationActionData, doAction);

        return createOperationExecutionPlan(operation);
    }

    private ExecutionPlan createOperationExecutionPlan(Operation operation) {
        ExecutionPlan executionPlan = new ExecutionPlan();
        executionPlan.setLanguage(SLANG_NAME);
        executionPlan.setBeginStep(1L);

        executionPlan.addStep(stepFactory.createStartStep(1L, operation.getPreOpActionData()));
        executionPlan.addStep(stepFactory.createActionStep(2L, operation.getDoAction().getActionData()));
        executionPlan.addStep(stepFactory.createEndStep(3L, operation.getPostOpActionData()));
        return executionPlan;
    }


    private DoAction compileAction(Map<String, Object> actionRawData) {
        Map<String, Serializable> actionData = new HashMap<>();

        Iterator it = actionRawData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            boolean wasTransformed = false;
            String key = pairs.getKey().toString();
            List<Transformer> actionTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Scope.ACTION)), transformers);
            for (Transformer transformer : actionTransformers) {
                if (shouldApplyTransformer(transformer, key)) {
                    Object value = transformer.transform(actionRawData.get(key));
                    actionData.put(key, (Serializable) value);
                    wasTransformed = true;
                }
            }
            it.remove();

            if (!wasTransformed) throw new RuntimeException("no transformer was found for: " + pairs.getKey());
        }
        return new DoAction(actionData);
    }

    private ExecutionPlan compileFlow(Map<String, Object> flowRawData, Map<String, ExecutionPlan> dependencies) {
        ExecutionPlan executionPlan = compileOperation(flowRawData, dependencies);
        executionPlan.setName((String) flowRawData.remove(SlangTextualKeys.FLOW_NAME_KEY));
        return executionPlan;
    }

    private boolean shouldApplyTransformer(Transformer transformer, String key) {
        String transformerName = transformer.getClass().getSimpleName().toLowerCase();
        return transformerName.startsWith(key.toLowerCase());
    }

}
