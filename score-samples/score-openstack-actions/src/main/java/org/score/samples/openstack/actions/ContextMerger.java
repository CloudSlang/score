package org.score.samples.openstack.actions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.score.lang.ExecutionRuntimeServices;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private static final String DEFAULT_HOST = "16.59.58.200"; //todo remove near hardcoded strings
	private static final String IDENTITY_PORT_KEY = "identityPort";
	private static final String COMPUTE_PORT_KEY = "computePort";
	private static final String DEFAULT_IMAGEREF = "56ff0279-f1fb-46e5-93dc-fe7093af0b1a";
	private static final String URL_KEY = "url";
	private static final String BODY_KEY = "body";
	private static final String HEADERS_KEY = "headers";
	private static final String HOST_KEY = "host";
	private static final String RETURN_RESULT_KEY = "returnResult";
	private static final String METHOD_KEY = "method";
	public final static String ACTION_RUNTIME_EVENT_TYPE = "ACTION_RUNTIME_EVENT";
	public final static String ACTION_EXCEPTION_EVENT_TYPE = "ACTION_EXCEPTION_EVENT";
	public static final String SERVERS_KEY = "returnResult";

	public Map<String, String> getServerNames(String returnResult){
		Map<String, String> returnMap = new HashMap<>();
		List<String> serverNames = getServerList(returnResult);

		String result = "";
		logger.info("Available servers:");

		for(String currentServerName : serverNames) {
			logger.info(currentServerName);
			result += currentServerName + ",";
		}
		returnMap.put(SERVERS_KEY, result);
		return returnMap;
	}
	public Map<String, String> prepareGetServerId(Map<String, Serializable> executionContext, ExecutionRuntimeServices executionRuntimeServices, String methodName){
		String returnResult = executionContext.get(RETURN_RESULT_KEY).toString();

		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Method \"" + methodName + "\" invoked.." +
				" Attempting to merge back results: " + returnResult);

		Map<String, String> returnMap = new HashMap<>();

		String token = getToken(returnResult);
		String tenant = getTenant(returnResult);

		returnMap.put("Token", token);
		returnMap.put("Tenant", tenant);

		executionContext.putAll(returnMap);
		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Results merged back in the Execution Context");
		return returnMap;
	}
	
	public Map<String, String> prepareGetServer(Map<String, Serializable> executionContext,ExecutionRuntimeServices executionRuntimeServices, String methodName){//, String returnResult, String host, String methodName) {

		String returnResult = executionContext.get(RETURN_RESULT_KEY).toString();
		String host = executionContext.get(HOST_KEY).toString();
		String computePort = executionContext.get(COMPUTE_PORT_KEY).toString();

		if (StringUtils.isEmpty(host)) {
			host = DEFAULT_HOST;
		}
		if(StringUtils.isEmpty(computePort)){
			computePort = DEFAULT_COMPUTE_PORT;
		}
		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Method \"" + methodName + "\" invoked.." +
				" Attempting to merge back results: " + returnResult);

		Map<String, String> returnMap = new HashMap<>();

		String token = getToken(returnResult);
		String tenant = getTenant(returnResult);

		String url = "http://" + host + ":" + computePort + "/v2/" + tenant + "/servers";
		returnMap.put(URL_KEY, url);
		returnMap.put(METHOD_KEY, "get");
		returnMap.put(HEADERS_KEY, "X-AUTH-TOKEN: " + token);

		returnMap.put("Token", token);
		returnMap.put("Tenant", tenant);

		executionContext.putAll(returnMap);
		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Results merged back in the Execution Context");
		return returnMap;
	}


	public Map<String, String> prepareDeleteServer(Map<String, Serializable> executionContext, ExecutionRuntimeServices executionRuntimeServices, String methodName){
		String returnResult = executionContext.get(RETURN_RESULT_KEY).toString();

		String serverName = executionContext.get("serverName").toString();
		String host = executionContext.get(HOST_KEY).toString();
		String computePort = executionContext.get(COMPUTE_PORT_KEY).toString();

		String tenant = executionContext.get("Tenant").toString();
		String token = executionContext.get("Token").toString();

		if(StringUtils.isEmpty(host)){
			host = DEFAULT_HOST;
		}
		if(StringUtils.isEmpty(computePort)){
			computePort = DEFAULT_COMPUTE_PORT;
		}

		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Method \"" + methodName + "\" invoked.." +
				" Attempting to merge back results: " + returnResult);

		Map<String, String> returnMap = new HashMap<>();


		String url = "http://" + host + ":" + computePort + "/v2/" + tenant + "/servers/" + returnResult;
		returnMap.put(URL_KEY, url);
		returnMap.put(METHOD_KEY, "delete");
		returnMap.put(HEADERS_KEY, "X-AUTH-TOKEN: " + token);


		executionContext.putAll(returnMap);
		executionContext.remove("body");

		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Results merged back in the Execution Context");
		return returnMap;

	}
	public Map<String, String> prepareCreateServer(Map<String, Serializable> executionContext, ExecutionRuntimeServices executionRuntimeServices, String methodName) {

		String returnResult = executionContext.get(RETURN_RESULT_KEY).toString();
		String serverName = executionContext.get("serverName").toString();
		String host = executionContext.get(HOST_KEY).toString();
		String computePort = executionContext.get(COMPUTE_PORT_KEY).toString();
		String imageRef = executionContext.get("imageRef").toString();


		if(StringUtils.isEmpty(host)){
			host = DEFAULT_HOST;
		}
		if(StringUtils.isEmpty(computePort)){
			computePort = DEFAULT_COMPUTE_PORT;
		}
		if(StringUtils.isEmpty(imageRef)){
			imageRef = DEFAULT_IMAGEREF;
		}

		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Method \"" + methodName + "\" invoked.." +
				" Attempting to merge back results: " + returnResult);

		Map<String, String> returnMap = new HashMap<>();

		String token = getToken(returnResult);
		String tenant = getTenant(returnResult);

		String url = "http://" + host + ":" + computePort + "/v2/" + tenant + "/servers";
		String body = "";

		body += "{\"server\": {\"name\": \"" +serverName + "\",";
		body += "\"imageRef\": \""+imageRef+"\",";
		body += "\"flavorRef\": \"2\",";
		body += "\"max_count\": 1,";
		body += "\"min_count\": 1,";
		body += "\"security_groups\": [{\"name\": \"default\"}]}}";

		returnMap.put(URL_KEY, url);
		returnMap.put(HEADERS_KEY, "X-AUTH-TOKEN: " + token);
		returnMap.put(BODY_KEY, body);

		executionContext.putAll(returnMap);
		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Results merged back in the Execution Context");

		return returnMap;
	}

	public List<String> getServerList(String returnResult){
		List<String> serverNames = new ArrayList<>();

		JsonElement parsedServerList = new JsonParser().parse(returnResult);
		JsonObject  serverListObject = parsedServerList.getAsJsonObject();
		JsonArray servers = serverListObject.getAsJsonArray("servers");

		for(int i = 0; i < servers.size(); ++i){
			serverListObject = servers.get(i).getAsJsonObject();
			String currentServerName = serverListObject.get("name").toString();
			currentServerName = currentServerName.substring(1, currentServerName.length()-1);
			serverNames.add(currentServerName);
		}
		return serverNames;
	}

	public Map<String, String> getServerId(Map<String, Serializable> executionContext, String returnResult, String serverName){
		Map<String, String> returnMap = new HashMap<>();
		JsonElement parsedServerList = new JsonParser().parse(returnResult);
		JsonObject  serverListObject = parsedServerList.getAsJsonObject();
		JsonArray servers = serverListObject.getAsJsonArray("servers");

		for(int i = 0; i < servers.size(); ++i){
			serverListObject = servers.get(i).getAsJsonObject();
			String currentServerName = serverListObject.get("name").toString();
			currentServerName = currentServerName.substring(1, currentServerName.length()-1);
			if(currentServerName.equals(serverName)){
				String serverId = serverListObject.get("id").toString();
				serverId = serverId.substring(1, serverId.length()-1);
				returnMap.put("returnResult", serverId);
				break;
			}
		}
		executionContext.putAll(returnMap);
		if(returnMap.containsKey("returnResult")) {

			return returnMap;
		}
		else returnMap.put("returnResult", "");

		return returnMap;
	}
	
	public String getTenant(String returnResult) {
		JsonElement parsedResult = new JsonParser().parse(returnResult);
		JsonObject parsedObject = parsedResult.getAsJsonObject();
		JsonObject accessObject = parsedObject.getAsJsonObject("access");
		JsonObject tokenObject = accessObject.getAsJsonObject("token");
		JsonObject tenantObject = tokenObject.getAsJsonObject("tenant");
		String resultTenant = tenantObject.get("id").toString();
		resultTenant = resultTenant.substring(1, resultTenant.length()-1);
		return resultTenant;
	}
	public String getToken(String returnResult) {
		JsonElement parsedResult = new JsonParser().parse(returnResult);
		JsonObject parsedObject = parsedResult.getAsJsonObject();
		JsonObject accessObject = parsedObject.getAsJsonObject("access");
		JsonObject tokenObject = accessObject.getAsJsonObject("token");
		String resultToken = tokenObject.get("id").toString();
		resultToken = resultToken.substring(1, resultToken.length()-1);
		return resultToken;
	}

	public Map<String, String> prepareStringOccurrences(String returnResult, String serverName) {
		Map<String, String> returnMap = new HashMap<>();
		returnMap.put("container", returnResult);
		returnMap.put("toFind", serverName);
		returnMap.put("ignoreCase", "true");
		return returnMap;
	}

	public void prepareSendEmail(Map<String, Serializable> executionContext, ExecutionRuntimeServices executionRuntimeServices) {

		if(!executionContext.containsKey("emailHost")) {
			executionContext.put("host", "smtp-americas.hp.com"); //todo remove near hardcoded strings
		}
		else {
			executionContext.put("host", executionContext.get("emailHost"));
		}
		if(!executionContext.containsKey("emailPort")){
			executionContext.put("port", "25");
		}
		else {
			executionContext.put("port", executionContext.get("emailHost"));
		}

		executionContext.put("from", "meshi.peer@hp.com"); //todo remove near hardcoded strings


		executionContext.put("subject", "OpenStack failure");
		executionContext.put("body", "Failure in OpenStack flows");

		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "prepareSendEmail");
	}
}
