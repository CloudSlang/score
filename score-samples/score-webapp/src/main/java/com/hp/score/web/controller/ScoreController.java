package com.hp.score.web.controller;

import com.hp.score.samples.FlowMetadata;
import com.hp.score.samples.openstack.actions.InputBinding;
import com.hp.score.web.ScoreService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Date: 8/29/2014
 *
 * @author Bonczidai Levente
 */
@RestController
public class ScoreController {
	private static String EXCEPTION_HTML_RESULT = "An exception occurred during the execution";

	private boolean flowRunning = false;
	private boolean newExecution = false;
	private String flowTextOutput = "";
	private ScoreService scoreService;

	@RequestMapping("/score")
	public ResponseEntity<String> options() {
		String options =
				"Available API calls:<BR>"
						+ "<BR>/list"
						+ "<BR>/run/*";
		return new ResponseEntity<>(options, null, HttpStatus.OK);
	}

	@RequestMapping("/score/list")
	public ResponseEntity<String> listFlows() {
		String options =
				"Usage: /score/run/{flow_identifier}<BR><BR>"
				+ "Available flows:<BR>"
				+ "<table>"
				+ "<tr><th>Flow description</th><th>Flow identifier</th></tr>";
		List<FlowMetadata> predefinedFlowIdentifiers = scoreService.getPredefinedFlowIdentifiers();
		for (FlowMetadata flowMetadata : predefinedFlowIdentifiers) {
			options += "<tr><td>" + flowMetadata.getDescription() + "</td><td>" + flowMetadata.getIdentifier() + "</td>" +
					"<td><a href=\"/score/run/" + flowMetadata.getIdentifier() + "\">run flow</a></td></tr>";
		}
		options += "</table>";
		return new ResponseEntity<>(options, null, HttpStatus.OK);
	}

	@RequestMapping(value = "/score/run/*")
	public ResponseEntity<String> runFlows(HttpServletRequest request) {
		//get flow identifier from url
		String uri = request.getRequestURI();
		String identifier = uri.split("/")[3];

		String inputForm;
		//get inputBindings
		try {
			List<InputBinding> bindings = scoreService.getInputBindingsByIdentifier(identifier);
			//generate input form
			inputForm = generateFlowInputsForm(bindings, identifier);
			newExecution = true;
		} catch (Exception e) {
			e.printStackTrace();
			inputForm = EXCEPTION_HTML_RESULT;
		}
		return new ResponseEntity<>(inputForm, null, HttpStatus.OK);
	}

	@RequestMapping("/score/trigger")
	public ResponseEntity<String> triggerFlows(HttpServletRequest request) {
		String response;
		HttpStatus httpStatus = HttpStatus.OK;
		try {
			if (!flowRunning && newExecution) {
				boolean requiredInputsMissing = false;
				String identifier = request.getParameter("identifier");
				//get the parameters from url
				List<InputBinding> inputBindings = scoreService.getInputBindingsByIdentifier(identifier);
				for (InputBinding inputBinding : inputBindings) {
					String parameter = request.getParameter(inputBinding.getSourceKey());
					if (parameter.isEmpty()) {
						requiredInputsMissing = true;
					}
					inputBinding.setValue(parameter);
				}
				if (requiredInputsMissing) {
					response = generateRequiredInputsRedirection(identifier);
					httpStatus = HttpStatus.BAD_REQUEST;
				} else {
					//this method also sets the flow running status
					scoreService.triggerWithBindings(identifier, inputBindings);
					newExecution = false;
					response =  generateTriggerPage(flowRunning, flowTextOutput);
				}
			} else {
				response =  generateTriggerPage(flowRunning, flowTextOutput);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response = EXCEPTION_HTML_RESULT;
			httpStatus = HttpStatus.BAD_REQUEST;
		}
		return new ResponseEntity<>(response, null, httpStatus);
	}

	public void setFlowRunning(boolean flowRunning) {
		if (flowRunning) {
			flowTextOutput = "";
		}
		this.flowRunning = flowRunning;
	}

	public void addTextOutput(String message) {
		flowTextOutput += "<BR>" + message;
	}

	public void setScoreService(ScoreService scoreService) {
		this.scoreService = scoreService;
	}

	private String generateRequiredInputsRedirection(String identifier) {
		return
		"<!DOCTYPE html>\n" +
		"<html>\n" +
		"    <head>\n" +
		"        <meta http-equiv=\"refresh\" content=\"2;url=/score/run/" + identifier + "\">\n" +
		"    </head>\n" +
		"<body>\n" +
		"Please provide all the required inputs!\n" +
		"</body>\n" +
		"</html>";
	}

	private String generateFlowInputsForm(List<InputBinding> bindings, String identifier){
		String inputsPage =
				"<!DOCTYPE html>\n" +
				"<html>\n" +
				"<body>\n" +
				"\n" +
				"<form name=\"input\" action=\"/score/trigger\" method=\"get\">\n" +
				"<input type=\"hidden\" name=\"identifier\" value=\"" + identifier + "\">\n";
		for (InputBinding inputBinding : bindings) {
			String defaultValue = "";
			if (inputBinding.hasDefaultValue()) {
				defaultValue = inputBinding.getValue();
			}
			String required = "";
			if (inputBinding.isRequired()) {
				required = "required input";
			}
			inputsPage += inputBinding.getDescription() + ": <input type=\"text\" name=\"" + inputBinding.getSourceKey() + "\" value=\"" + defaultValue + "\"> &nbsp&nbsp" + required +"<br>\n";
		}
		inputsPage +=
				"<input type=\"submit\" value=\"Submit\">\n" +
				"</form>\n" +
				"\n" +
				"</body>\n" +
				"</html>";
		return inputsPage;
	}

	private String generateTriggerPage(boolean withAutoRefresh, String flowTextOutput){
		String refreshMetaTag = "        <meta http-equiv=\"refresh\" content=\"2\" />\n";
		String response =
				"<!DOCTYPE html>\n" +
				"<html>\n" +
				"    <head>\n";
		if (withAutoRefresh) {
			response += refreshMetaTag;
		}
		response +=
				"    </head>\n"
				+ "<body>\n"
				+ "Flow events:<BR>"
				+ flowTextOutput
				+		"</body>\n"
				+				"</html>";
		return response;
	}
}
