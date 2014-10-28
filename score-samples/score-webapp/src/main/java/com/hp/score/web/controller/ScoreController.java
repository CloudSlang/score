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
package com.hp.score.web.controller;

import com.google.gson.*;
import com.hp.score.samples.FlowMetadata;
import com.hp.score.samples.openstack.actions.InputBinding;
import com.hp.score.web.services.ScoreServices;
import com.mysema.commons.lang.Assert;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Date: 8/29/2014
 *
 * @author Bonczidai Levente
 */
@RestController
public class ScoreController {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    private static final Logger logger = Logger.getLogger(ScoreController.class);

    private static final String IDENTIFIER_KEY = "identifier";
    private static final String API_KEY = "api";
    private static final String LIST_KEY = "list";
    private static final String RUN_KEY = "runs";
    private static final String NAME_KEY = "name";
    private static final String DESCRIPTION_KEY = "description";
    private static final String FLOWS_KEY = "flows";
    private static final String REQUIRED_KEY = "required";
    private static final String INPUTS_KEY = "inputs";
    private static final String EXECUTION_ID_KEY = "execution id";
    private static final String VALUE_KEY = "value";
    private static final String V1 = "v1";

    private static final String API_URI = "/" + V1 + "/" + API_KEY;
    private static final String LIST_URI = "/" + V1 + "/" + FLOWS_KEY + "/" + LIST_KEY;
    private static final String INPUTS_URI_WITH_IDENTIFIER = "/" + V1 + "/" + FLOWS_KEY + "/{" + IDENTIFIER_KEY + "}" + "/" + INPUTS_KEY;
    private static final String INPUTS_URI_WITH_NAME ="/" + V1 + "/" + FLOWS_KEY + "/{" + NAME_KEY + "}" + "/" + INPUTS_KEY;
    private static final String RUN_URI_IDENTIFIER = "/" + V1 + "/" + RUN_KEY;

    private ScoreServices scoreServices;

	@RequestMapping(value = API_URI, method= RequestMethod.GET)
	public ResponseEntity<String> getAvailableAPICalls() {
        JsonArray apiList = new JsonArray();
        apiList.add(new JsonPrimitive(LIST_URI));
        apiList.add(new JsonPrimitive(INPUTS_URI_WITH_IDENTIFIER));
        apiList.add(new JsonPrimitive(INPUTS_URI_WITH_NAME));
        apiList.add(new JsonPrimitive(RUN_URI_IDENTIFIER));

        JsonObject api = new JsonObject();
        api.add(API_KEY, apiList);

		return new ResponseEntity<>(gson.toJson(api), null, HttpStatus.OK);
	}

	@RequestMapping(value = LIST_URI, method= RequestMethod.GET)
	public ResponseEntity<String> listFlows() {
        JsonArray flowsArray = new JsonArray();
        List<FlowMetadata> predefinedFlowsMetadata = scoreServices.getPredefinedFlowsMetadata();
        for (FlowMetadata flowMetadata : predefinedFlowsMetadata) {
            JsonObject flowData = new JsonObject();
            flowData.addProperty(IDENTIFIER_KEY, flowMetadata.getIdentifier());
            flowData.addProperty(NAME_KEY, flowMetadata.getName());
            flowData.addProperty(DESCRIPTION_KEY, flowMetadata.getDescription());
            flowsArray.add(flowData);
        }

        JsonObject flows = new JsonObject();
        flows.add(FLOWS_KEY, flowsArray);

		return new ResponseEntity<>(gson.toJson(flows), null, HttpStatus.OK);
	}

    @RequestMapping(value = INPUTS_URI_WITH_IDENTIFIER, method= RequestMethod.GET)
    public ResponseEntity<String> getFlowInputs(@PathVariable String identifier) {
        // identifier can mean either the flow identifier or the flow name depends on request
        JsonObject flowInfo = new JsonObject();
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            JsonArray inputArray = new JsonArray();
            List<InputBinding> bindings = scoreServices.getInputBindingsByIdentifierOrName(identifier);
            for (InputBinding inputBinding : bindings) {
                JsonObject input = new JsonObject();
                input.addProperty(NAME_KEY, inputBinding.getSourceKey());
                if (inputBinding.hasDefaultValue()) {
                    input.addProperty(VALUE_KEY, inputBinding.getValue());
                }
                input.addProperty(REQUIRED_KEY, inputBinding.isRequired());
                inputArray.add(input);
            }
            flowInfo.add(INPUTS_KEY, inputArray);
        } catch (Exception e) {
            e.printStackTrace();
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(gson.toJson(flowInfo), null, httpStatus);
    }

	@RequestMapping(value= RUN_URI_IDENTIFIER, method= RequestMethod.POST)
	public ResponseEntity<String> createFlowRun(@RequestBody String inputsAsJson) {
        JsonObject triggerInfo = new JsonObject();
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            String identifierOrName = fetchIdentifierOrNameFromJson(inputsAsJson);
            List<InputBinding> bindings = fetchInputsFromJson(inputsAsJson, identifierOrName);
            long executionId = scoreServices.triggerWithBindings(identifierOrName, bindings);
            triggerInfo.addProperty(EXECUTION_ID_KEY, executionId);
        }
        catch(Exception ex) {
            triggerInfo.addProperty(EXECUTION_ID_KEY, -1);
            httpStatus = HttpStatus.BAD_REQUEST;
            logger.error(ex);
        }
        return new ResponseEntity<>(gson.toJson(triggerInfo), null, httpStatus);
	}

    private String fetchIdentifierOrNameFromJson(String inputsAsJson) throws Exception {
        JsonParser jsonParser = new JsonParser();
        JsonObject bodyAsJson = jsonParser.parse(inputsAsJson).getAsJsonObject();
        if (bodyAsJson.has(IDENTIFIER_KEY)) {
            return bodyAsJson.get(IDENTIFIER_KEY).getAsString();
        } else {
            if (bodyAsJson.has(NAME_KEY)) {
                return bodyAsJson.get(NAME_KEY).getAsString();
            } else {
                throw new Exception("Identifier / name not found in Json body");
            }
        }
    }

    private List<InputBinding> fetchInputsFromJson(String inputsAsJson, String identifier) throws Exception {
        JsonParser jsonParser = new JsonParser();
        JsonObject bodyAsJson = jsonParser.parse(inputsAsJson).getAsJsonObject();
        JsonElement inputs = bodyAsJson.get(INPUTS_KEY);
        JsonArray inputArray = inputs.getAsJsonArray();
        List<InputBinding> bindings = scoreServices.getInputBindingsByIdentifierOrName(identifier);

        for (JsonElement input : inputArray) {
            JsonObject inputAsJsonObject = input.getAsJsonObject();
            String sourceKey = inputAsJsonObject.entrySet().iterator().next().getKey();
            String value = inputAsJsonObject.get(sourceKey).getAsString();

            int inputCount = 0;
            for (InputBinding inputBinding : bindings) {
                if (inputBinding.getSourceKey().equals(sourceKey)) {
                    ++inputCount;
                    inputBinding.setValue(value);
                }
            }

            Assert.isTrue(inputCount == 1, "Invalid input \"" + sourceKey + "\"");
        }

        return bindings;
    }

    public void setScoreServices(ScoreServices scoreServices) {
        this.scoreServices = scoreServices;
    }
}
