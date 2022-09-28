package org.openpaas.servicebroker.util;

import java.util.Map;

public class JsonUtil {
	static public String convertToJson(Map<String, String> map) {

		StringBuilder builder = new StringBuilder();
		builder.append("{");

		for ( String key : map.keySet()) {
			builder.append("\"" + key + "\"" + ":" + "\"" + map.get(key) + "\"");
			builder.append(",");
		}

		builder.deleteCharAt(builder.lastIndexOf(","));

		builder.append("}");

		return builder.toString();
	}
}
