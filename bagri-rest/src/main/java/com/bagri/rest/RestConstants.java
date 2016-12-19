package com.bagri.rest;

public class RestConstants {

	public static final String bg_cookie = "bg-auth";
	
	public static final String DELETE = "DELETE";
	public static final String GET = "GET";
	public static final String HEAD = "HEAD";
	public static final String OPTIONS = "OPTIONS";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String TRACE = "TRACE";
	
    public static final transient String[] methods = {GET, POST, PUT, DELETE};
	
	public static final String an_path = "rest:path";
	public static final String an_consumes = "rest:consumes";
	public static final String an_produces = "rest:produces";
	
	public static final String apn_cookie = "rest:cookie-param";
	public static final String apn_form = "rest:form-param";
	public static final String apn_header = "rest:header-param";
	public static final String apn_matrix = "rest:matrix-param";
	public static final String apn_query = "rest:query-param";

}
