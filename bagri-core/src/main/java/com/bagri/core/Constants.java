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
	public static final String pn_client_txLevel = "bdb.client.txLevel";
	public static final String pn_client_txTimeout = "bdb.client.txTimeout";

	public static final String pv_client_txLevel_skip = "skip";
	
	public static final String pn_client_smart = "bdb.client.smart";
	public static final String pn_client_bufferSize = "bdb.client.bufferSize";
	public static final String pn_client_connectAttempts = "bdb.client.connectAttempts";
	public static final String pn_client_poolSize = "bdb.client.poolSize";

	public static final String pn_client_connectedAt = "bdb.client.connectedAt"; 
	public static final String pn_client_memberId = "bdb.client.member"; 
	public static final String pn_client_sharedConnection = "bdb.client.sharedConnection"; 

	public static final String pn_client_fetchAsynch = "bdb.client.fetchAsynch";
	public static final String pn_client_fetchSize = "bdb.client.fetchSize";
	public static final String pn_client_fetchType = "bdb.client.fetchType";

	public static final String pv_client_fetchType_asynch = "asynch";
	public static final String pv_client_fetchType_fixed = "fixed";
	public static final String pv_client_fetchType_queued = "queued";
	
	public static final String pn_client_healthCheck = "bdb.client.healthCheck";
	public static final String pn_client_loginTimeout = "bdb.client.loginTimeout";
	public static final String pn_client_dataFactory = "bdb.client.dataFactory";
	public static final String pn_client_pageSize = "bdb.client.pageSize";
	
	public static final String pn_client_customAuth = "bdb.client.customAuth";
	public static final String pn_client_queryCache = "bdb.client.queryCache";
	public static final String pn_client_storeMode = "bdb.client.storeMode";
	public static final String pn_client_submitTo = "bdb.client.submitTo";
	public static final String pn_client_ownerParam = "bdb.client.ownerParam";

	public static final String pv_client_storeMode_insert = "insert";
	public static final String pv_client_storeMode_update = "update";
	public static final String pv_client_storeMode_merge = "merge";
	
	public static final String pv_client_submitTo_all = "all";
	public static final String pv_client_submitTo_any = "any";
	public static final String pv_client_submitTo_query_key_owner = "query-key-owner";
	public static final String pv_client_submitTo_param_hash_owner = "param-hash-owner";
	public static final String pv_client_submitTo_param_value_owner = "param-value-owner";

	public static final String pn_client_contentSerializer = "bdb.client.contentSerializer";
	public static final String pn_client_contentSerializers = "bdb.client.contentSerializers";
	public static final String pv_client_defaultSerializers = "MAP BMAP SMAP JSON XML";
	
    public static final String pn_document_collections = "bdb.document.collections";
    public static final String pn_document_compress = "bdb.document.compress";
    public static final String pn_document_data_format = "bdb.document.data.format";
    public static final String pn_document_headers = "bdb.document.headers";
    public static final String pn_document_data_source = "bdb.document.data.source";
    public static final String pn_document_map_merge = "bdb.document.map.merge";
    public static final String pn_document_cache_content = "bdb.document.cache.content";
    public static final String pn_document_cache_elements = "bdb.document.cache.elements";
    
    public static final String pv_document_data_source_file = "FILE"; 
    public static final String pv_document_data_source_map = "MAP"; 
    public static final String pv_document_data_source_json = "JSON"; 
    public static final String pv_document_data_source_xml = "XML"; 

	public static final String pn_query_command = "bdb.query.command";
	public static final String pn_query_customPaths = "bdb.query.customPaths";
	public static final String pn_query_customQuery = "bdb.query.customQuery";
	public static final String pn_query_splitBy = "bdb.query.splitBy";
	public static final String pn_query_invalidate = "bdb.query.invalidate";
	public static final String pn_query_updateable = "bdb.query.updateable";
	
	public static final String pv_query_invalidate_none = "none";
	public static final String pv_query_invalidate_docs = "docs";
	public static final String pv_query_invalidate_paths = "paths";
	public static final String pv_query_invalidate_values = "values";
	public static final String pv_query_invalidate_all = "all";
	
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
    public static final String pn_rest_accept_pool = "bdb.rest.accept.pool";
    public static final String pn_rest_thread_pool = "bdb.rest.thread.pool";
    
    public static final String pn_access_filename = "bdb.access.filename";
    public static final String pn_config_filename = "bdb.config.filename";
    public static final String pn_config_path = "bdb.config.path";
    public static final String pn_config_context_file = "bdb.config.context.file";
    public static final String pn_config_properties_file = "bdb.config.properties.file";

    public static final String pn_log_level = "bdb.log.level";
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
    public static final String pn_schema_publish_counters = "bdb.schema.publish.counters";
    public static final String pn_schema_query_cache = "bdb.schema.query.cache";
    public static final String pn_schema_tx_level = "bdb.schema.transaction.level";
    public static final String pn_schema_tx_timeout = "bdb.schema.transaction.timeout";
    
    public static final String pn_schema_builder_pretty = "bdb.schema.builder.pretty";
    public static final String pn_schema_builder_ident = "bdb.schema.builder.ident";
    public static final String pn_schema_parser_schemas = "bdb.schema.parser.schemas";
    
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
	public static final String bg_prefix = bg_schema + ":";
	public static final String bg_ns = "http://bagridb.com/bdb";

	public static final String bg_version = "2.0.0-SNAPSHOT";
	
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
	public static final String cmd_get_document_uris = "get-document-uris";
	public static final String cmd_query_document_uris = "query-document-uris";
	public static final String cmd_remove_document = "remove-document";
	public static final String cmd_remove_cln_documents = "remove-collection-documents";
	public static final String cmd_store_document = "store-document";
	
	// updating functions
	public static final String bg_remove_document = bg_prefix + cmd_remove_document;
	public static final String bg_remove_cln_documents = bg_prefix + cmd_remove_cln_documents;
	public static final String bg_store_document = bg_prefix + cmd_store_document;
	
	public static int propToInt(String property) {
		switch (property) {
			case pn_schema_address: return 1;
			case pn_schema_host: return 2;
			case pn_schema_port: return 3;
			case pn_schema_name: return 4;
			case pn_schema_user: return 5;
			case pn_schema_password: return 6;
			case pn_client_id: return 7;
			case pn_client_txId: return 8;
			case pn_client_txLevel: return 9;
			case pn_client_txTimeout: return 10;
			case pn_client_smart: return 11;
			case pn_client_bufferSize: return 12;
			case pn_client_connectAttempts: return 13;
			case pn_client_poolSize: return 14;
			case pn_client_connectedAt: return 15; 
			case pn_client_memberId: return 16; 
			case pn_client_fetchAsynch: return 17;
			case pn_client_fetchSize: return 18;
			case pn_client_fetchType: return 19;
			case pn_client_healthCheck: return 20;
			case pn_client_loginTimeout: return 21;
			case pn_client_dataFactory: return 22;
			case pn_client_pageSize: return 23;
			case pn_client_customAuth: return 24;
			case pn_client_queryCache: return 25;
			case pn_client_storeMode: return 26;
			case pn_client_submitTo: return 27;
			case pn_client_ownerParam: return 28;
			case pn_client_sharedConnection: return 29;
			
			case pn_query_command: return 40;
			case pn_query_customPaths: return 41;
			case pn_query_customQuery: return 42;
			case pn_query_splitBy: return 43;
			case pn_query_invalidate: return 44;
			case pn_query_updateable: return 45;

			case pn_document_collections: return 50;
			case pn_document_data_format: return 51;
			case pn_document_headers: return 52;
			case pn_document_data_source: return 53;
			case pn_document_map_merge: return 54;
			case pn_document_compress: return 55;
			case pn_document_cache_content: return 56;
			case pn_document_cache_elements: return 57;
			
			case pn_xqj_baseURI: return 100;
			case pn_xqj_constructionMode: return 101;
			case pn_xqj_defaultCollationUri: return 102;
			case pn_xqj_defaultElementTypeNamespace: return 103;
			case pn_xqj_defaultFunctionNamespace: return 104;
			case pn_xqj_orderingMode: return 105;
			case pn_xqj_defaultOrderForEmptySequences: return 106;
			case pn_xqj_boundarySpacePolicy: return 107;
			case pn_xqj_copyNamespacesModePreserve: return 108;
			case pn_xqj_copyNamespacesModeInherit: return 109;
			case pn_xqj_bindingMode: return 110;
			case pn_xqj_queryLanguageTypeAndVersion: return 111;
			case pn_xqj_holdability: return 112;
			case pn_xqj_scrollability: return 113;
			case pn_xqj_queryTimeout: return 114;
			case pn_xqj_defaultNamespaces: return 115;
		}
		return 0;
	}
	
	public static String intToProp(int idx) {
		switch (idx) {
			case 1: return pn_schema_address;
			case 2: return pn_schema_host;
			case 3: return pn_schema_port;
			case 4: return pn_schema_name;
			case 5: return pn_schema_user;
			case 6: return pn_schema_password;
			case 7: return pn_client_id;
			case 8: return pn_client_txId;
			case 9: return pn_client_txLevel;
			case 10: return pn_client_txTimeout;
			case 11: return pn_client_smart;
			case 12: return pn_client_bufferSize;
			case 13: return pn_client_connectAttempts;
			case 14: return pn_client_poolSize;
			case 15: return pn_client_connectedAt; 
			case 16: return pn_client_memberId; 
			case 17: return pn_client_fetchAsynch;
			case 18: return pn_client_fetchSize;
			case 19: return pn_client_fetchType;
			case 20: return pn_client_healthCheck;
			case 21: return pn_client_loginTimeout;
			case 22: return pn_client_dataFactory;
			case 23: return pn_client_pageSize;
			case 24: return pn_client_customAuth;
			case 25: return pn_client_queryCache;
			case 26: return pn_client_storeMode;
			case 27: return pn_client_submitTo;
			case 28: return pn_client_ownerParam;
			case 29: return pn_client_sharedConnection;

			case 40: return pn_query_command;
			case 41: return pn_query_customPaths;
			case 42: return pn_query_customQuery;
			case 43: return pn_query_splitBy;
			case 44: return pn_query_invalidate;
			case 45: return pn_query_updateable;
			
			case 50: return pn_document_collections;
			case 51: return pn_document_data_format;
			case 52: return pn_document_headers;
			case 53: return pn_document_data_source;
			case 54: return pn_document_map_merge;
			case 55: return pn_document_compress;
			case 56: return pn_document_cache_content;
			case 57: return pn_document_cache_elements;
			
			case 100: return pn_xqj_baseURI;
			case 101: return pn_xqj_constructionMode;
			case 102: return pn_xqj_defaultCollationUri;
			case 103: return pn_xqj_defaultElementTypeNamespace;
			case 104: return pn_xqj_defaultFunctionNamespace;
			case 105: return pn_xqj_orderingMode;
			case 106: return pn_xqj_defaultOrderForEmptySequences;
			case 107: return pn_xqj_boundarySpacePolicy;
			case 108: return pn_xqj_copyNamespacesModePreserve;
			case 109: return pn_xqj_copyNamespacesModeInherit;
			case 110: return pn_xqj_bindingMode;
			case 111: return pn_xqj_queryLanguageTypeAndVersion;
			case 112: return pn_xqj_holdability;
			case 113: return pn_xqj_scrollability;
			case 114: return pn_xqj_queryTimeout;
			case 115: return pn_xqj_defaultNamespaces;
		}
		return null;
	}
	
}
