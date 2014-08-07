package org.score.samples.openstack.actions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
	private static final String TENANT  = "1ef9a1495c774e969ad6de86e6f025d7";

	public Map<String, String> prepareGetServer(String returnResult) {
		String host = "16.59.58.200";
		String port = "8774";
		String token = getToken(returnResult);
		Map<String, String> returnMap = new HashMap<>();
		String url = "http://" + host + ":" + port + "/v2/" + TENANT + "/servers";
		returnMap.put("url", url);
		returnMap.put("method", "get");
		String headers = "X-AUTH-TOKEN: " + token;
		returnMap.put("headers", headers);
		return returnMap;
	}

	public Map<String, String> prepareCreateServer(String returnResult, String serverName) {
		Map<String, String> returnMap = new HashMap<>();
		String token = getToken(returnResult);

		returnMap.put("url", "http://16.59.58.200:8774/v2/" + TENANT + "/servers");
		returnMap.put("headers", "X-AUTH-TOKEN: " + token);
		returnMap.put("body", "{\"server\": {\"name\": \"" + serverName + "\",\"imageRef\": \"56ff0279-f1fb-46e5-93dc-fe7093af0b1a\",\"flavorRef\": \"2\",\"max_count\": 1,\"min_count\": 1,\"security_groups\": [{\"name\": \"default\"}]}}");
		returnMap.put("result", "0");

		return returnMap;
	}

	private String getToken(String returnResult) {
		JsonElement jelement = new JsonParser().parse(returnResult);
		JsonObject jobject = jelement.getAsJsonObject();
		jobject = jobject.getAsJsonObject("access");
		jobject = jobject.getAsJsonObject("token");
		String result = jobject.get("id").toString();
		result = result.substring(1, result.length()-1);
		return result;
	}

	public Map<String, String> getServerNames(String returnResult){
		Map<String, String> returnMap = new HashMap<>();
		List<String> serverNames = new ArrayList<>();

		JsonElement jelement = new JsonParser().parse(returnResult);
		JsonObject  jobject = jelement.getAsJsonObject();
		JsonArray jarray = jobject.getAsJsonArray("servers");

		for(int i = 0; i < jarray.size(); ++i){
			jobject = jarray.get(i).getAsJsonObject();
			serverNames.add(jobject.get("name").toString());
		}
		System.out.println("Available servers:");
		for(String currentServerName : serverNames) {
			System.out.println(currentServerName);
		}
		returnMap.put("result", "0");
		return returnMap;
	}
}
