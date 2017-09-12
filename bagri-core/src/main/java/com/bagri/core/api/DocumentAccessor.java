package com.bagri.core.api;

import java.util.Map;
import java.util.Properties;

public interface DocumentAccessor {
	
	public static String HDR_COLLECTIONS = "COLLECTIONS";
	public static String HDR_CONTENT_TYPE = "CONTENT_TYPE";
	public static String HDR_CREATED_AT = "CREATED_AT";
	public static String HDR_CREATED_BY = "CREATED_BY";
	public static String HDR_ENCODING = "ENCODING";
	public static String HDR_FORMAT = "FORMAT";
	public static String HDR_KEY = "DOCUMENT_KEY";
	public static String HDR_SIZE_IN_BYTES = "BYTES";
	public static String HDR_SIZE_IN_ELEMENTS = "ELEMENTS";
	public static String HDR_SIZE_IN_FRAGMENTS = "FRAGMENTS";
	public static String HDR_TYPE_ROOT = "TYPE_ROOT";
	public static String HDR_TX_START = "TX_START";
	public static String HDR_TX_FINISH = "TX_FINISH";
	public static String HDR_URI = "URI";
	public static String HDR_VERSION = "VERSION";
	
	String getUri();
	
	int getVersion();
	
	<T> T getContent();
	
	Properties getProperties();
	
	String getProperty(String pName);
	
	Map<String, Object> getHeaders();
	
	<T> T getHeader(String hName);

}
