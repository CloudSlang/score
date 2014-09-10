package com.hp.score.samples.openstack.actions;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.oo.sdk.content.annotations.Param;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




/**
 * Date: 9/2/2014
 *
 * @author lesant
 */

public class OpenstackUtils {
	private static final String RETURN_RESULT_KEY = "returnResult";
	private static final String NAME_KEY = "name";
	private static final String ID_KEY = "id";
	private static final String TOKEN_KEY = "token";
	private static final String TENANT_KEY = "tenant";
	private static final String ACCESS_KEY = "access";
	public static final String SERVERS_KEY = "servers";
	public static final String RETURN_CODE = "returnCode";
	public static final String SUCCESS_CODE = "0";
	public static final String FAILED_CODE = "1";
	public static final String RESPONSE_KEY = "response";
	public static final String SUCCESS_RESPONSE = "success";
	public static final String FAILURE_RESPONSE = "failure";



	private final static Logger logger = Logger.getLogger(OpenstackUtils.class);

	@SuppressWarnings("unused")
	public Map<String, String> getServerNames(@Param("getServersResponse") String getServersResponse) {
		Map<String, String> returnMap = new HashMap<>();
		List<String> serverNames = getServerList(getServersResponse);

		String result = "";
		logger.info("Available servers:");

		for(String currentServerName : serverNames) {
			System.out.println(currentServerName);
			result += currentServerName + ",";
		}
		returnMap.put(RETURN_RESULT_KEY, result);

		return returnMap;
	}
	public List<String> getServerList(String getServersResponse){
		List<String> serverNames = new ArrayList<>();

		JsonElement parsedServerList = new JsonParser().parse(getServersResponse);
		JsonObject serverListObject = parsedServerList.getAsJsonObject();
		JsonArray servers = serverListObject.getAsJsonArray(SERVERS_KEY);

		for(int i = 0; i < servers.size(); ++i){
			serverListObject = servers.get(i).getAsJsonObject();
			String currentServerName = serverListObject.get(NAME_KEY).toString();
			currentServerName = currentServerName.substring(1, currentServerName.length()-1);
			serverNames.add(currentServerName);
		}
		return serverNames;
	}
	@SuppressWarnings("unused")
	public Map<String, String> getServerId(@Param("getServersResponse") String getServersResponse,
                                           @Param("serverName") String serverName){
		Map<String, String> returnMap = new HashMap<>();
		JsonElement parsedServerList = new JsonParser().parse(getServersResponse);
		JsonObject  serverListObject = parsedServerList.getAsJsonObject();
		JsonArray servers = serverListObject.getAsJsonArray(SERVERS_KEY);

		for(int i = 0; i < servers.size(); ++i){
			serverListObject = servers.get(i).getAsJsonObject();
			String currentServerName = serverListObject.get(NAME_KEY).toString();
			currentServerName = currentServerName.substring(1, currentServerName.length()-1);
			if(currentServerName.equals(serverName)){
				String serverId = serverListObject.get(ID_KEY).toString();
				serverId = serverId.substring(1, serverId.length()-1);
				returnMap.put(RETURN_RESULT_KEY, serverId);
				break;
			}
		}
		if(returnMap.containsKey(RETURN_RESULT_KEY)) {
			returnMap.put(RETURN_CODE, SUCCESS_CODE);
			return returnMap;
		}

		returnMap.put(RETURN_RESULT_KEY, "");
		returnMap.put(RETURN_CODE, FAILED_CODE);
		return returnMap;
	}
	@SuppressWarnings("unused")
	public Map<String, String> parseAuthentication(@Param("jsonAuthenticationResponse") String jsonAuthenticationResponse) { 
		Map<String, String> returnMap = new HashMap<>();
		JsonElement parsedResult = new JsonParser().parse(jsonAuthenticationResponse);
		JsonObject parsedObject = parsedResult.getAsJsonObject();
		JsonObject accessObject = parsedObject.getAsJsonObject(ACCESS_KEY);
		JsonObject tokenObject = accessObject.getAsJsonObject(TOKEN_KEY);

		String resultToken = tokenObject.get(ID_KEY).toString();
		resultToken = resultToken.substring(1, resultToken.length()-1);

		JsonObject tenantObject = tokenObject.getAsJsonObject(TENANT_KEY);
		String resultTenant = tenantObject.get(ID_KEY).toString();
		resultTenant = resultTenant.substring(1, resultTenant.length() - 1);

		returnMap.put("parsedTenant", resultTenant);
		returnMap.put("parsedToken", resultToken);
		returnMap.put(RETURN_RESULT_KEY, "Parsing successful. Results put in the Execution Context");
		if (!(StringUtils.isEmpty(resultToken) && StringUtils.isEmpty(resultTenant))) {
			returnMap.put(RETURN_CODE, SUCCESS_CODE);
		}
		else{
			returnMap.put(RETURN_CODE, FAILED_CODE);
		}
		return returnMap;

	}
	@SuppressWarnings("unused")
	public Map<String, String> getMultiInstanceResponse(@Param("branchResults") List<Map<String,Serializable>> branchResults){
		Boolean failure = false;
		Map<String, String> returnMap = new HashMap<>();

		for(Map<String, Serializable> currentBranchContext : branchResults){
			if(StringUtils.equals(currentBranchContext.get(RESPONSE_KEY).toString(), FAILURE_RESPONSE)){
				failure = true;
			}
		}
		if(failure){
			returnMap.put(RESPONSE_KEY, FAILURE_RESPONSE);
		} else {
			returnMap.put(RESPONSE_KEY, SUCCESS_RESPONSE);
		}
		return returnMap;
	}

	@SuppressWarnings("unused")
	public void splitServersIntoBranchContexts(Map<String, Serializable> executionContext, String serverNamesList){
		String[] serverNames = StringUtils.split(serverNamesList, ',');

		List<Map<String, Serializable>> branchContexts = new ArrayList<>();
		for(String currentServerName : serverNames){
			Map<String, Serializable> currentBranchContext = new HashMap<>();
			currentBranchContext.putAll(executionContext);
			currentBranchContext.put("serverName", currentServerName);
			branchContexts.add(currentBranchContext);
		}
		executionContext.put("branchContexts", (Serializable) branchContexts);
	}
}
