package com.hp.score.web.controller;

import com.google.gson.*;
import com.hp.score.samples.FlowMetadata;
import com.hp.score.samples.openstack.actions.InputBinding;
import com.hp.score.web.ScoreHelper;

import com.mysema.commons.lang.Assert;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    private static final String ROOT_KEY = "score";
    private static final String LIST_KEY = "list";
    private static final String INFO_KEY = "info";
    private static final String TRIGGER_KEY = "trigger";
    private static final String NAME_KEY = "name";
    private static final String DESCRIPTION_KEY = "description";
    private static final String FLOWS_KEY = "flows";
    private static final String REQUIRED_KEY = "required";
    private static final String INPUTS_KEY = "inputs";
    private static final String EXECUTION_ID_KEY = "execution id";
    private static final String VALUE_KEY = "value";

    private ScoreHelper scoreHelper;

	@RequestMapping(value = "/" + ROOT_KEY + "/" + API_KEY, method= RequestMethod.GET)
	public ResponseEntity<String> options() {
        JsonArray apiList = new JsonArray();
        apiList.add(new JsonPrimitive("/" + LIST_KEY));
        apiList.add(new JsonPrimitive("/" + INFO_KEY + "/{" + IDENTIFIER_KEY + "}"));
        apiList.add(new JsonPrimitive("/" + TRIGGER_KEY +"/{" + IDENTIFIER_KEY + "}"));

        JsonObject api = new JsonObject();
        api.add(API_KEY, apiList);

		return new ResponseEntity<>(gson.toJson(api), null, HttpStatus.OK);
	}

	@RequestMapping(value = "/" + ROOT_KEY + "/" + LIST_KEY, method= RequestMethod.GET)
	public ResponseEntity<String> listFlows() {
        JsonArray flowsArray = new JsonArray();
        List<FlowMetadata> predefinedFlowsMetadata = scoreHelper.getPredefinedFlowsMetadata();
        for (FlowMetadata flowMetadata : predefinedFlowsMetadata) {
            JsonObject flowData = new JsonObject();
            flowData.addProperty(IDENTIFIER_KEY, flowMetadata.getIdentifier());
            flowData.addProperty(DESCRIPTION_KEY, flowMetadata.getDescription());
            flowsArray.add(flowData);
        }

        JsonObject flows = new JsonObject();
        flows.add(FLOWS_KEY, flowsArray);

		return new ResponseEntity<>(gson.toJson(flows), null, HttpStatus.OK);
	}

    @RequestMapping(value = "/" + ROOT_KEY + "/" + INFO_KEY + "/{" + IDENTIFIER_KEY + "}", method= RequestMethod.GET)
    public ResponseEntity<String> getFlowInputs(HttpServletRequest request, @PathVariable String identifier) {
        JsonObject flowInfo = new JsonObject();
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            flowInfo.addProperty(IDENTIFIER_KEY, identifier);
            JsonArray inputArray = new JsonArray();
            List<InputBinding> bindings = scoreHelper.getInputBindingsByIdentifier(identifier);
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

	@RequestMapping(value= "/" + ROOT_KEY + "/" + TRIGGER_KEY + "/{" + IDENTIFIER_KEY + "}", method= RequestMethod.POST)
	public ResponseEntity<String> triggerFlows(@PathVariable String identifier, @RequestBody String inputsAsJson) {
        JsonObject triggerInfo = new JsonObject();
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            List<InputBinding> bindings = fetchInputsFromJson(inputsAsJson, identifier);
            long executionId = scoreHelper.triggerWithBindings(identifier, bindings);
            triggerInfo.addProperty(EXECUTION_ID_KEY, executionId);
        }
        catch(Exception ex) {
            triggerInfo.addProperty(EXECUTION_ID_KEY, -1);
            httpStatus = HttpStatus.BAD_REQUEST;
            logger.error(ex);
        }
        return new ResponseEntity<>(gson.toJson(triggerInfo), null, httpStatus);
	}

    private List<InputBinding> fetchInputsFromJson(String inputsAsJson, String identifier) throws Exception {
        JsonParser jsonParser = new JsonParser();
        JsonObject bodyAsJson = jsonParser.parse(inputsAsJson).getAsJsonObject();
        JsonElement inputs = bodyAsJson.get(INPUTS_KEY);
        JsonArray inputArray = inputs.getAsJsonArray();
        List<InputBinding> bindings = scoreHelper.getInputBindingsByIdentifier(identifier);

        for (JsonElement input : inputArray) {
            String sourceKey = input.getAsJsonObject().get(NAME_KEY).getAsString();
            String value = input.getAsJsonObject().get(VALUE_KEY).getAsString();

            int inputCount = 0;
            for (InputBinding inputBinding : bindings) {
                if (inputBinding.getSourceKey().equals(sourceKey)) {
                    ++inputCount;
                    inputBinding.setValue(value);
                }
            }

            Assert.isTrue(inputCount == 1, "Expected number of inputs with the given name is 1 but actual number is " + inputCount);
        }

        return bindings;
    }

    public void setScoreHelper(ScoreHelper scoreHelper) {
        this.scoreHelper = scoreHelper;
    }
}
