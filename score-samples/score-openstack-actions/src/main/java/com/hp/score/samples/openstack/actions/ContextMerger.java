package com.hp.score.samples.openstack.actions;


import com.hp.score.lang.ExecutionRuntimeServices;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;



import java.io.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Date: 8/5/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class ContextMerger {

	private final static Logger logger = Logger.getLogger(ContextMerger.class);

	private static final String DEFAULT_COMPUTE_PORT = "8774";
	private static final String DEFAULT_IDENTITY_PORT = "5000";
	private static final String IDENTITY_PORT_KEY = "identityPort";
	private static final String COMPUTE_PORT_KEY = "computePort";
	private static final String PORT_KEY = "port";
	private static final String DEFAULT_IMAGEREF = "56ff0279-f1fb-46e5-93dc-fe7093af0b1a";
	private static final String ID_KEY = "id";
	private static final String IMAGEREF_KEY = "imgRef";
	private static final String URL_KEY = "url";
	private static final String BODY_KEY = "body";
	private static final String HEADERS_KEY = "headers";
	private static final String HOST_KEY = "host";
	private static final String RETURN_RESULT_KEY = "returnResult";
	private static final String METHOD_KEY = "method";
	private static final String TOKEN_KEY = "token";
	private static final String TENANT_KEY = "tenant";
	private static final String SERVER_NAME_KEY = "serverName";
	private static final String ACCESS_KEY = "access";
	private static final String NAME_KEY = "name";
	private static final String SUBJECT_KEY = "subject";
	private static final String EMAIL_HOST_KEY = "emailHost";
	private static final String EMAIL_PORT_KEY = "emailPort";
	public final static String ACTION_RUNTIME_EVENT_TYPE = "ACTION_RUNTIME_EVENT";
	public final static String ACTION_EXCEPTION_EVENT_TYPE = "ACTION_EXCEPTION_EVENT";
	public static final String SERVERS_KEY = "servers";
	private static final String FLOW_DESCRIPTION = "flowDescription";
	public static final String USERNAME_KEY = "username";
	public static final String IMAGE_REFERENCE_KEY = "imgRef";
	public static final String CONTENT_TYPE_KEY = "contentType";
	public static final String IMAGE_REF_KEY = "imageRef";
	public static final String PREPARE_SEND_EMAIL_METHOD = "prepareSendEmail";
	public final static Integer STRING_START_INDEX = 0;
	public final static Integer STRING_END_INDEX = 80;

	public String replacePlaceHolders(Map<String, Serializable> executionContext, String inputValue){
		StringBuffer sb = new StringBuffer();
		Matcher m = Pattern.compile("\\$\\{(.*?)\\}").matcher(inputValue);

		while (m.find()) {
			String placeHolderKey = m.group(1); //toReplace
			if( executionContext.containsKey(placeHolderKey)) {
				String placeHolderValue = executionContext.get(placeHolderKey).toString(); //toInsert
				m.appendReplacement(sb, "" + placeHolderValue);
			}
			else{
				m.appendReplacement(sb, "");
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}
	public Map<String, String> merge(List<InputBinding> inputs, Map<String, Serializable> executionContext, ExecutionRuntimeServices executionRuntimeServices, String methodName){
		if(executionContext.containsKey(RETURN_RESULT_KEY)) {
			String returnResult = executionContext.get(RETURN_RESULT_KEY).toString();

			executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Method \"" + methodName + "\" invoked.." +
					" Attempting to merge back results: " + returnResult);
		}
		Map<String, String> returnMap = new HashMap<>();

		String inputValue;

		for(InputBinding binding : inputs){

			if(StringUtils.isEmpty(binding.getSourceKey())){
				inputValue = binding.getValue();
			}else {
				inputValue = executionContext.get(binding.getSourceKey()).toString();
			}
			String actualValue = replacePlaceHolders(executionContext, inputValue);

			returnMap.put(binding.getDestinationKey(), actualValue);
		}
		executionContext.putAll(returnMap);

		if(executionContext.containsKey(RETURN_RESULT_KEY)) {
			executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Results merged back in the Execution Context");
		}

		return returnMap;
	}
	
	public void validateServerResult(Map<String, Serializable> executionContext, ExecutionRuntimeServices executionRuntimeServices){
		executionContext.put(RETURN_RESULT_KEY, "The specified server (" +executionContext.get(SERVER_NAME_KEY) + ") was not created or server already exists.");
	}
}
