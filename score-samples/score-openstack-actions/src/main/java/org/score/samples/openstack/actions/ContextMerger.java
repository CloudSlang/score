package org.score.samples.openstack.actions;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;


/**
 * Date: 8/5/2014
 *
 * @author lesant
 */
@SuppressWarnings("unused")
public class ContextMerger {
	public Map<String, String> merge(String returnResult,  String serverName) {

		Map<String, String> returnMap = new HashMap<>();

		JsonElement jelement = new JsonParser().parse(returnResult);
		JsonObject jobject = jelement.getAsJsonObject();
		jobject = jobject.getAsJsonObject("access");
		jobject = jobject.getAsJsonObject("token");
		String result = jobject.get("id").toString();


		returnMap.put("url", "http://16.59.58.200:8774/v2/1ef9a1495c774e969ad6de86e6f025d7/servers");
		returnMap.put("headers", "X-AUTH-TOKEN: " + result.substring(1, result.length()-1));
		returnMap.put("body", "{\"server\": {\"name\": \""+ serverName+"\",\"imageRef\": \"56ff0279-f1fb-46e5-93dc-fe7093af0b1a\",\"flavorRef\": \"2\",\"max_count\": 1,\"min_count\": 1,\"security_groups\": [{\"name\": \"default\"}]}}");
		returnMap.put("result", "0");




		return returnMap;
	}
}
