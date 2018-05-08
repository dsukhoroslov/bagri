package com.bagri.support.util;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtils {

	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Serialize POJO to JSON string
	 * 
	 * @param bean the POJO to serialize
	 * @return the serialization result
	 */
	public static String beanToJSON(Object bean) {
		try {
			return mapper.writeValueAsString(bean);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException(ex);
		}
	}	
	
	/**
	 * Deserialize POJO from JSON string
	 * 
	 * @param json the JSON to deserialize
	 * @param cls the class of bean to return
	 * @param <T> the class parameter of bean to return
	 * @return the deserialization result
	 */
	public static <T> Object beanFromJSON(String json, Class<T> cls) {
		try {
			return mapper.readValue(json, cls);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	// should do Map mapping via custom mapper, probably..
	
	/**
	 * Serialize Map to JSON string
	 * 
	 * @param map the Map to serialize
	 * @return the serialization result
	 */
	public static String mapToJSON(Map<String, Object> map) {
		try {
			return mapper.writeValueAsString(map);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Deserialize Map from JSON string
	 * 
	 * @param json the JSON to deserialize
	 * @return the deserialization result
	 */
	public static Map<String, Object> mapFromJSON(String json) {
		try {
			return mapper.readValue(json, Map.class);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
}
