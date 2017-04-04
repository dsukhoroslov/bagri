package com.bagri.core;

/**
 * Connection (client-side) and configuration (server-side) properties and standard values.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class Constants {

	// XDM Client configuration constants
	public static final String pn_schema_address = "bdb.schema.address";
	public static final String pn_schema_host = "bdb.schema.host";
	public static final String pn_schema_port = "bdb.schema.port";
	public static final String pn_schema_name = "bdb.schema.name";
	public static final String pn_schema_user = "bdb.schema.user";
	public static final String pn_schema_password = "bdb.schema.password";

	public static final String pn_client_id = "bdb.client.id";
	public static final String pn_client_txId = "bdb.client.txId";
	public static final String pn_client_txTimeout = "bdb.client.txTimeout";
	
	public static final String pn_client_smart = "bdb.client.smart";
	public static final String pn_client_bufferSize = "bdb.client.bufferSize";
	public static final String pn_client_connectAttempts = "bdb.client.connectAttempts";
	public static final String pn_client_poolSize = "bdb.client.poolSize";

	public static final String pn_client_connectedAt = "bdb.client.connectedAt"; 
	public static final String pn_client_memberId = "bdb.client.member"; 

	public static final String pn_client_fetchSize = "bdb.client.fetchSize";
	public static final String pn_client_healthCheck = "bdb.client.healthCheck";
	public static final String pn_client_loginTimeout = "bdb.client.loginTimeout";
	public static final String pn_client_dataFactory = "bdb.client.dataFactory";
	
	public static final String pn_client_customAuth = "bdb.client.customAuth";
	public static final String pn_client_queryCache = "bdb.client.queryCache";
	public static final String pn_client_storeMode = "bdb.client.storeMode";
	public static final String pn_client_submitTo = "bdb.client.submitTo";

	public static final String pv_client_storeMode_insert = "insert";
	public static final String pv_client_storeMode_update = "update";
	public static final String pv_client_storeMode_merge = "merge";
	
	public static final String pv_client_submitTo_any = "any";
	public static final String pv_client_submitTo_member = "member";
	public static final String pv_client_submitTo_owner = "owner";
	
	public static final String pn_query_command = "bdb.query.command";
	
	// XDM Server configuration constants
	public static final String pn_cluster_login = "bdb.cluster.login";
    public static final String pn_cluster_node_name = "bdb.cluster.node.name";
    public static final String pn_cluster_node_role = "bdb.cluster.node.role";
    public static final String pn_cluster_node_size = "bdb.cluster.node.size";
    public static final String pn_cluster_admin_port = "bdb.cluster.admin.port";
    public static final String pn_cluster_node_schemas = "bdb.cluster.node.schemas";

	// REST Server configuration constants
	public static final String pn_rest_enabled = "bdb.rest.enabled";
    public static final String pn_rest_jmx = "bdb.rest.jmx";
    public static final String pn_rest_port = "bdb.rest.port";
    public static final String pn_rest_auth_port = "bdb.rest.auth.port";
    
    public static final String pn_access_filename = "bdb.access.filename";
    public static final String pn_config_filename = "bdb.config.filename";
    public static final String pn_config_path = "bdb.config.path";
    public static final String pn_config_context_file = "bdb.config.context.file";
    public static final String pn_config_properties_file = "bdb.config.properties.file";

    public static final String pn_node_instance = "bdb.node.instance";
    
    public static final String pn_spring_context = "bdb.spring.context";
    
    public static final String pn_schema_members = "bdb.schema.members";
    public static final String pn_schema_ports_first = "bdb.schema.ports.first";
    public static final String pn_schema_ports_last = "bdb.schema.ports.last";
    public static final String pn_schema_format_default = "bdb.schema.format.default";
    public static final String pn_schema_store_type = "bdb.schema.store.type";
    public static final String pn_schema_store_enabled = "bdb.schema.store.enabled";
    public static final String pn_schema_store_data_path = "bdb.schema.store.data.path";
    public static final String pn_schema_store_tx_buffer_size = "bdb.schema.store.tx.buffer.size";
    public static final String pn_schema_store_read_only = "bdb.schema.store.read-only";
    public static final String pn_schema_population_size = "bdb.schema.population.size";
    public static final String pn_schema_population_buffer_size = "bdb.schema.population.buffer.size";
    public static final String pn_schema_fetch_size = "bdb.schema.fetch.size";
    public static final String pn_schema_query_cache = "bdb.schema.query.cache";
    
    public static final String pn_document_collections = "bdb.document.collections";
    public static final String pn_document_data_format = "bdb.document.data.format";

	// XQJ configuration constants
	public static final String pn_xqj_baseURI = "xqj.schema.baseUri";
	public static final String pn_xqj_constructionMode = "xqj.schema.constructionMode";
	public static final String pn_xqj_defaultCollationUri = "xqj.schema.defaultCollationUri";
	public static final String pn_xqj_defaultElementTypeNamespace = "xqj.schema.defaultElementTypeNamespace";
	public static final String pn_xqj_defaultFunctionNamespace = "xqj.schema.defaultFunctionNamespace";
	public static final String pn_xqj_orderingMode = "xqj.schema.orderingMode";
	public static final String pn_xqj_defaultOrderForEmptySequences = "xqj.schema.defaultOrderForEmptySequences";
	public static final String pn_xqj_boundarySpacePolicy = "xqj.schema.boundarySpacePolicy";
	public static final String pn_xqj_copyNamespacesModePreserve = "xqj.schema.copyNamespacesModePreserve";
	public static final String pn_xqj_copyNamespacesModeInherit = "xqj.schema.copyNamespacesModeInherit";
	public static final String pn_xqj_bindingMode = "xqj.schema.bindingMode";
	public static final String pn_xqj_queryLanguageTypeAndVersion = "xqj.schema.queryLanguageTypeAndVersion";
	public static final String pn_xqj_holdability = "xqj.schema.holdability";
	public static final String pn_xqj_scrollability = "xqj.schema.scrollability";
	public static final String pn_xqj_queryTimeout = "xqj.schema.queryTimeout";
	public static final String pn_xqj_defaultNamespaces = "xqj.schema.defaultNamespaces";

    // schemas, namespaces
	public static final String xs_prefix = "xs";
	public static final String xs_ns = "http://www.w3.org/2001/XMLSchema";

	public static final String bg_schema = "bgdb";
	public static final String bg_ns = "http://bagridb.com/bdb";

	public static final String bg_version = "1.1.2";
	
	public static final String dc_ns = "http://www.w3.org/2005/xpath-functions/collation/codepoint";
	public static final String df_ns = "http://www.w3.org/2005/xpath-functions";
	
	public static final String mt_xml = "application/xml"; 
	public static final String mt_json = "application/json";
	
	// DocumentStore initialization context constants
	public static final String ctx_repo = "xdmRepository";
	public static final String ctx_cache = "cacheInstance";
	public static final String ctx_context = "userContext";
	public static final String ctx_popService = "popManager";
	
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
	   
	// direct commands
	public static final String cmd_get_document = "get-document-content";
	public static final String cmd_remove_document = "remove-document";
	public static final String cmd_remove_cln_documents = "remove-collection-documents";
	public static final String cmd_store_document = "store-document";
	
	// updating functions
	public static final String bg_remove_document = bg_schema + ":" + cmd_remove_document;
	public static final String bg_remove_cln_documents = bg_schema + ":" + cmd_remove_cln_documents;
	public static final String bg_store_document = bg_schema + ":" + cmd_store_document;
	
}
