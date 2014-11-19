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
package org.eclipse.score.web.controller;

import org.eclipse.score.samples.FlowMetadata;
import org.eclipse.score.samples.openstack.actions.InputBinding;
import org.eclipse.score.web.NotFoundException;
import org.eclipse.score.web.services.ScoreServices;
import com.google.gson.*;
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
    private static final String RUN_KEY = "runs";
    private static final String NAME_KEY = "name";
    private static final String DESCRIPTION_KEY = "description";
    private static final String FLOWS_KEY = "flows";
    private static final String REQUIRED_KEY = "required";
    private static final String INPUTS_KEY = "inputs";
    private static final String VALUE_KEY = "value";
    private static final String V1 = "v1";
    private static final String SCORE_PREFIX = "score";

    private static final String API_URI = "/" + SCORE_PREFIX + "/" + V1 + "/" + API_KEY;
    private static final String LIST_URI = "/" + SCORE_PREFIX + "/" + V1 + "/" + FLOWS_KEY;
    private static final String INPUTS_URI_WITH_IDENTIFIER = "/" + SCORE_PREFIX + "/" + V1 + "/" + FLOWS_KEY + "/{" + IDENTIFIER_KEY + "}" + "/" + INPUTS_KEY;
    private static final String RUN_URI_IDENTIFIER = "/" + SCORE_PREFIX + "/" + V1 + "/" + RUN_KEY;

    private ScoreServices scoreServices;

	@RequestMapping(value = API_URI, method= RequestMethod.GET)
	public ResponseEntity<String> getAvailableAPICalls() {
        JsonArray apiList = new JsonArray();
        apiList.add(new JsonPrimitive(LIST_URI));
        apiList.add(new JsonPrimitive(INPUTS_URI_WITH_IDENTIFIER));
        apiList.add(new JsonPrimitive(RUN_URI_IDENTIFIER));

		return new ResponseEntity<>(gson.toJson(apiList), null, HttpStatus.OK);
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

		return new ResponseEntity<>(gson.toJson(flowsArray), null, HttpStatus.OK);
	}

    @RequestMapping(value = INPUTS_URI_WITH_IDENTIFIER, method= RequestMethod.GET)
    public ResponseEntity<String> getFlowInputs(@PathVariable String identifier) {
        HttpStatus httpStatus = HttpStatus.OK;
        JsonArray inputArray = new JsonArray();
        String responseBody = "";
        try {
            List<InputBinding> bindings = scoreServices.getInputBindingsByIdentifier(identifier);
            for (InputBinding inputBinding : bindings) {
                JsonObject input = new JsonObject();
                input.addProperty(NAME_KEY, inputBinding.getSourceKey());
                if (inputBinding.hasDefaultValue()) {
                    input.addProperty(VALUE_KEY, inputBinding.getValue());
                }
                input.addProperty(REQUIRED_KEY, inputBinding.isRequired());
                inputArray.add(input);
                responseBody = gson.toJson(inputArray);
            }
        } catch (NotFoundException nfex) {
            logger.error(nfex.getMessage());
            httpStatus = HttpStatus.NOT_FOUND;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(responseBody, null, httpStatus);
    }

	@RequestMapping(value= RUN_URI_IDENTIFIER, method= RequestMethod.POST)
	public ResponseEntity<String> runFlow(@RequestBody String inputsAsJson) {
        String responseBody = "";
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            String identifier = fetchIdentifierFromJson(inputsAsJson);
            List<InputBinding> bindings = fetchInputsFromJson(inputsAsJson, identifier);
            long executionId = scoreServices.triggerWithBindings(identifier, bindings);
            responseBody = String.valueOf(executionId);
        } catch (NotFoundException nfex) {
            logger.error(nfex.getMessage());
            httpStatus = HttpStatus.NOT_FOUND;
        } catch(Exception ex) {
            logger.error(ex.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(responseBody, null, httpStatus);
	}

    private String fetchIdentifierFromJson(String inputsAsJson) throws Exception {
        JsonParser jsonParser = new JsonParser();
        JsonObject bodyAsJson = jsonParser.parse(inputsAsJson).getAsJsonObject();
        if (bodyAsJson.has(IDENTIFIER_KEY)) {
            return bodyAsJson.get(IDENTIFIER_KEY).getAsString();
        } else {
            throw new Exception("Identifier not found in Json body");
        }
    }

    private List<InputBinding> fetchInputsFromJson(String inputsAsJson, String identifier) throws Exception {
        JsonParser jsonParser = new JsonParser();
        JsonObject bodyAsJson = jsonParser.parse(inputsAsJson).getAsJsonObject();
        JsonElement inputs = bodyAsJson.get(INPUTS_KEY);
        JsonArray inputArray = inputs.getAsJsonArray();
        List<InputBinding> bindings = scoreServices.getInputBindingsByIdentifier(identifier);

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
