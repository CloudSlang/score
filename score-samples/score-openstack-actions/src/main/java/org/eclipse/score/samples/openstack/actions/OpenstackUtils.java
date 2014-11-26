/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.samples.openstack.actions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
	private static final String PARSED_TOKEN_KEY = "parsedToken";
	private static final String PARSED_TENANT_KEY = "parsedTenant";
	private static final String ACCESS_KEY = "access";
	public static final String SERVERS_KEY = "servers";
	public static final String RETURN_CODE = "returnCode";
	public static final String SUCCESS_CODE = "0";
	public static final String FAILED_CODE = "1";
	public static final String RESPONSE_KEY = "response";
	public static final String SUCCESS_RESPONSE = "success";
	public static final String FAILURE_RESPONSE = "failure";
	public static final String BRANCH_RESULTS_KEY = "branchResults";
	public static final String BRANCH_CONTEXTS_KEY = "branchContexts";
	public static final String GET_SERVERS_RESPONSE_KEY = "getServersResponse";
	public static final String SERVER_NAME_KEY = "serverName";
	public static final String JSON_AUTHENTICATION_RESPONSE_KEY= "jsonAuthenticationResponse";
	private final static Logger logger = Logger.getLogger(OpenstackUtils.class);

	/**
	 * Prints servers names to console
	 *
	 * @param executionContext executionContext object populated by score
	 */
	@SuppressWarnings("unused")
	public void getServerNames(Map<String, Serializable> executionContext) {

		String getServersResponse = (String) executionContext.get(GET_SERVERS_RESPONSE_KEY);

		List<String> serverNames = getServerList(getServersResponse);

		String result = "";
		logger.info("Available servers:");

		for (String currentServerName : serverNames) {
			System.out.println(currentServerName);
			result += currentServerName + ",";
		}
		executionContext.put(RETURN_RESULT_KEY, result);

	}

	/**
	 * Returns a list of server names from the returnResult.
	 *
	 * @param getServersResponse returnResult from the getServers action
	 * @return return list of strings
	 */
	public List<String> getServerList(String getServersResponse) {
		List<String> serverNames = new ArrayList<>();

		JsonElement parsedServerList = new JsonParser().parse(getServersResponse);
		JsonObject serverListObject = parsedServerList.getAsJsonObject();
		JsonArray servers = serverListObject.getAsJsonArray(SERVERS_KEY);

		for (int i = 0; i < servers.size(); ++i) {
			serverListObject = servers.get(i).getAsJsonObject();
			String currentServerName = serverListObject.get(NAME_KEY).toString();
			currentServerName = currentServerName.substring(1, currentServerName.length() - 1);
			serverNames.add(currentServerName);
		}
		return serverNames;
	}

	/**
	 * Iterates through branchResults and overwrites the RESPONSE_KEY in the executionContext if one
	 * branch had the failure response.
	 *
	 * @param executionContext executionContext object populated by score
	 */
	@SuppressWarnings("unused")
	public void joinBranchResponses(Map<String, Serializable> executionContext) {
		Boolean failure = false;
		@SuppressWarnings("unchecked")
		List<Map<String, Serializable>> branchResults = (List<Map<String, Serializable>>) executionContext.get(BRANCH_RESULTS_KEY);

		for (Map<String, Serializable> currentBranchContext : branchResults) {
			if (StringUtils.equals(currentBranchContext.get(RESPONSE_KEY).toString(), FAILURE_RESPONSE)) {
				failure = true;
			}
		}
		if (failure) {
			executionContext.put(RESPONSE_KEY, FAILURE_RESPONSE);
		} else {
			executionContext.put(RESPONSE_KEY, SUCCESS_RESPONSE);
		}
	}

	/**
	 * Parses authentication response to get the Tenant and Token and puts them
	 * back in the executionContext.
	 *
	 * @param executionContext executionContext object populated by score
	 */
	@SuppressWarnings("unused")
	public void parseAuthentication(Map<String, Serializable> executionContext) {
		String jsonAuthenticationResponse = (String) executionContext.get(JSON_AUTHENTICATION_RESPONSE_KEY);

		JsonElement parsedResult = new JsonParser().parse(jsonAuthenticationResponse);
		JsonObject parsedObject = parsedResult.getAsJsonObject();
		JsonObject accessObject = parsedObject.getAsJsonObject(ACCESS_KEY);
		JsonObject tokenObject = accessObject.getAsJsonObject(TOKEN_KEY);

		String resultToken = tokenObject.get(ID_KEY).toString();
		resultToken = resultToken.substring(1, resultToken.length() - 1);

		JsonObject tenantObject = tokenObject.getAsJsonObject(TENANT_KEY);
		String resultTenant = tenantObject.get(ID_KEY).toString();
		resultTenant = resultTenant.substring(1, resultTenant.length() - 1);

		executionContext.put(PARSED_TENANT_KEY, resultTenant);
		executionContext.put(PARSED_TOKEN_KEY, resultToken);
		executionContext.put(RETURN_RESULT_KEY, "Parsing successful. Results put in the Execution Context");
		if (!(StringUtils.isEmpty(resultToken) && StringUtils.isEmpty(resultTenant))) {
			executionContext.put(RETURN_CODE, SUCCESS_CODE);
		} else {
			executionContext.put(RETURN_CODE, FAILED_CODE);
		}
	}

	/**
	 * Creates a list of Maps that will be used as branch contexts.
	 * Each one has a different server name stored under the serverName key.
	 *
	 * @param executionContext executionContext object populated by score
	 * @param serverNamesList list of strings corresponding to the names of servers that will be created
	 */
	@SuppressWarnings("unused")
	public void splitServersIntoBranchContexts(Map<String, Serializable> executionContext, String serverNamesList) {
		String[] serverNames = StringUtils.split(serverNamesList, ',');

		List<Map<String, Serializable>> branchContexts = new ArrayList<>();
		for (String currentServerName : serverNames) {
			Map<String, Serializable> currentBranchContext = new HashMap<>();
			currentBranchContext.putAll(executionContext);
			currentBranchContext.put(SERVER_NAME_KEY, currentServerName);
			branchContexts.add(currentBranchContext);
		}
		executionContext.put(BRANCH_CONTEXTS_KEY, (Serializable) branchContexts);
	}


	/**
	 * Parses getServerResponse from execution in order to get the Id of the serverName gave
	 * as an flow input.
	 *
	 * @param executionContext executionContext object populated by score
	 */
	@SuppressWarnings("unused")
	public void getServerId(Map<String, Serializable> executionContext) {
		String getServersResponse = (String) executionContext.get(GET_SERVERS_RESPONSE_KEY);
		String serverName = (String) executionContext.get(SERVER_NAME_KEY);

		JsonElement parsedServerList = new JsonParser().parse(getServersResponse);
		JsonObject serverListObject = parsedServerList.getAsJsonObject();
		JsonArray servers = serverListObject.getAsJsonArray(SERVERS_KEY); // all servers

		for (int i = 0; i < servers.size(); ++i) { // find the Id of the server with the corresponding name
			serverListObject = servers.get(i).getAsJsonObject();
			String currentServerName = serverListObject.get(NAME_KEY).toString();
			currentServerName = currentServerName.substring(1, currentServerName.length() - 1);
			if (currentServerName.equals(serverName)) {
				String serverId = serverListObject.get(ID_KEY).toString();
				serverId = serverId.substring(1, serverId.length() - 1); //gets rid of extra quotes
				executionContext.put(RETURN_RESULT_KEY, serverId); //puts result back in executionContext
				break;
			}
		}
		if (executionContext.containsKey(RETURN_RESULT_KEY)) { //also puts Return code depending if parsing
			executionContext.put(RETURN_CODE, SUCCESS_CODE);// was successful
		}
		else {
			executionContext.put(RETURN_RESULT_KEY, "");
			executionContext.put(RETURN_CODE, FAILED_CODE);
		}
	}
}
