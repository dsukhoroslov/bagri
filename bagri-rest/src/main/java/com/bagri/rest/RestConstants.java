package com.bagri.rest;

public class RestConstants {

	public static final String bg_cookie = "bg-auth";
	
	static final String DELETE = "DELETE";
	static final String GET = "GET";
	static final String HEAD = "HEAD";
	static final String OPTIONS = "OPTIONS";
	static final String POST = "POST";
	static final String PUT = "PUT";
	static final String TRACE = "TRACE";
	
    public static final transient String[] methods = {GET, POST, PUT, DELETE};
	
	public static final String an_path = "rest:path";
	public static final String an_consumes = "rest:consumes";
	public static final String an_produces = "rest:produces";
	
	static final String apn_cookie = "rest:cookie-param";
	static final String apn_form = "rest:form-param";
	static final String apn_header = "rest:header-param";
	static final String apn_matrix = "rest:matrix-param";
	static final String apn_query = "rest:query-param";

}
