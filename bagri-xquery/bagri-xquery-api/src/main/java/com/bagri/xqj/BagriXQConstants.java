package com.bagri.xqj;

public class BagriXQConstants {

	public static final String xs_prefix = "xs";
	public static final String xs_ns = "http://www.w3.org/2001/XMLSchema";

	public static final String pn_schema_name = "xdm.schema.name";
	public static final String pn_server_address = "xdm.schema.address";
	public static final String pn_schema_user = "xdm.schema.user";
	public static final String pn_schema_password = "xdm.schema.password";
	//public static final String pn_pool_size = "xdm.schema.poolSize";

	public static final String pn_client_smart = "xdm.client.smart";
	public static final String pn_data_factory = "xdm.client.dataFactory";
	public static final String pn_client_id = "xdm.client.id";
	public static final String pn_tx_id = "xdm.client.txId";
	public static final String pn_fetch_size = "xdm.client.fetchSize";
	public static final String pn_login_timeout = "xdm.client.loginTimeout";
	public static final String pn_client_submitTo = "xdm.client.submitTo";
	
	public static final String pn_baseURI = "xqj.schema.baseUri";
	public static final String pn_constructionMode = "xqj.schema.constructionMode";
	public static final String pn_defaultCollationUri="xqj.schema.defaultCollationUri";
	public static final String pn_defaultElementTypeNamespace = "xqj.schema.defaultElementTypeNamespace";
	public static final String pn_defaultFunctionNamespace = "xqj.schema.defaultFunctionNamespace";
	public static final String pn_orderingMode = "xqj.schema.orderingMode";
	public static final String pn_defaultOrderForEmptySequences = "xqj.schema.defaultOrderForEmptySequences";
	public static final String pn_boundarySpacePolicy = "xqj.schema.boundarySpacePolicy";
	public static final String pn_copyNamespacesModePreserve = "xqj.schema.copyNamespacesModePreserve";
	public static final String pn_copyNamespacesModeInherit = "xqj.schema.copyNamespacesModeInherit";
	public static final String pn_bindingMode = "xqj.schema.bindingMode";
	public static final String pn_queryLanguageTypeAndVersion = "xqj.schema.queryLanguageTypeAndVersion";
	public static final String pn_holdability = "xqj.schema.holdability";
	public static final String pn_scrollability = "xqj.schema.scrollability";
	public static final String pn_queryTimeout = "xqj.schema.queryTimeout";
	public static final String pn_defaultNamespaces = "xqj.schema.defaultNamespaces";
	
	public static final String bg_schema = "bgdm";
	public static final String bg_ns = "http://bagri.com/bagri-xdm";
	
	
	public static final String ex_connection_closed = "Connection is closed";
	public static final String ex_null_context = "Context is null";

	// XQJ MetaData feature constants
	public static final int xqf_Update = 1;
	public static final int xqf_XQueryX = 2;
	public static final int xqf_Transaction = 3;
	public static final int xqf_Full_Axis = 4;
	public static final int xqf_Schema_Import = 5;
	public static final int xqf_Schema_Validation = 6;
	public static final int xqf_Module = 7;
	public static final int xqf_Serialization = 8;
	public static final int xqf_Static_Typing = 9;
	public static final int xqf_Static_Typing_Extensions = 10;
	public static final int xqf_XQuery_Encoding_Decl = 11;
	public static final int xqf_User_Defined_XML_Schema_Type = 12;
	
	//   public int getMaxExpressionLength() throws XQException;
	//   public int getMaxUserNameLength() throws XQException;
	//   public boolean wasCreatedFromJDBCConnection() throws XQException;
	//   public java.util.Set getSupportedXQueryEncodings() throws XQException;
	//   public boolean isXQueryEncodingSupported(String encoding) throws XQException;
	
	// XQJ2 MetaData feature constants
	public static final int xqf_XQuery_Update_Facility = 13;
	public static final int xqf_XQuery_Full_Text = 14;
	public static final int xqf_XQuery_30 = 15;
	public static final int xqf_XA = 16;
	   
}
