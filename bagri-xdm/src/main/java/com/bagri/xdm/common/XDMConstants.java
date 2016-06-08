package com.bagri.xdm.common;

public class XDMConstants {

	public static final String xs_prefix = "xs";
	public static final String xs_ns = "http://www.w3.org/2001/XMLSchema";

	public static final String pn_schema_address = "xdm.schema.address";
	public static final String pn_schema_host = "xdm.schema.host";
	public static final String pn_schema_port = "xdm.schema.port";
	public static final String pn_schema_name = "xdm.schema.name";
	public static final String pn_schema_user = "xdm.schema.user";
	public static final String pn_schema_password = "xdm.schema.password";
	//public static final String pn_pool_size = "xdm.schema.poolSize";

	public static final String pn_client_id = "xdm.client.id";
	public static final String pn_client_txId = "xdm.client.txId";
	public static final String pn_client_txTimeout = "xdm.client.txTimeout";
	
	public static final String pn_client_smart = "xdm.client.smart";
	public static final String pn_client_bufferSize = "xdm.client.bufferSize";
	public static final String pn_client_connectAttempts = "xdm.client.connectAttempts";
	public static final String pn_client_poolSize = "xdm.client.poolSize";

	public static final String pn_client_connectedAt = "xdm.client.connectedAt"; 
	public static final String pn_client_memberId = "xdm.client.member"; 

	public static final String pn_client_fetchSize = "xdm.client.fetchSize";
	public static final String pn_client_healthCheck = "xdm.client.healthCheck";
	public static final String pn_client_loginTimeout = "xdm.client.loginTimeout";
	public static final String pn_client_dataFactory = "xdm.client.dataFactory";
	
	public static final String pn_client_customAuth = "xdm.client.customAuth";
	public static final String pn_client_queryCache = "xdm.client.queryCache";
	public static final String pn_client_storeMode = "xdm.client.storeMode";
	public static final String pn_client_submitTo = "xdm.client.submitTo";

	public static final String pv_client_storeMode_insert = "insert";
	public static final String pv_client_storeMode_update = "update";
	public static final String pv_client_storeMode_merge = "merge";
	
	public static final String pv_client_submitTo_any = "any";
	public static final String pv_client_submitTo_member = "member";
	public static final String pv_client_submitTo_owner = "owner";
	
	public static final String pn_query_command = "xdm.query.command";
	
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
	public static final String bg_ns = "http://bagridb.com/bagri-xdm";
	
	public static final String cmd_get_document = "get-document";
	public static final String cmd_remove_document = "remove-document";
	public static final String cmd_remove_cln_documents = "remove-collection-documents";
	public static final String cmd_store_document = "store-document";
	
	public static final String dc_ns = "http://www.w3.org/2005/xpath-functions/collation/codepoint";
	public static final String df_ns = "http://www.w3.org/2005/xpath-functions";
	
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
	   
	// XDM Configuration Constants
	public static final String xdm_cluster_login = "xdm.cluster.login";
    public static final String xdm_cluster_node_name = "xdm.cluster.node.name";
    public static final String xdm_cluster_node_role = "xdm.cluster.node.role";
    public static final String xdm_cluster_node_size = "xdm.cluster.node.size";
    public static final String xdm_cluster_admin_port = "xdm.cluster.admin.port";
    public static final String xdm_cluster_node_schemas = "xdm.cluster.node.schemas";

    public static final String xdm_access_filename = "xdm.access.filename";
    public static final String xdm_config_filename = "xdm.config.filename";
    public static final String xdm_config_path = "xdm.config.path";
    public static final String xdm_config_context_file = "xdm.config.context.file";
    public static final String xdm_config_properties_file = "xdm.config.properties.file";

    public static final String xdm_node_instance = "xdm.node.instance";
    
    public static final String xdm_spring_context = "xdm.spring.context";
    
    public static final String xdm_schema_name = "xdm.schema.name";
    public static final String xdm_schema_members = "xdm.schema.members";
    public static final String xdm_schema_ports_first = "xdm.schema.ports.first";
    public static final String xdm_schema_ports_last = "xdm.schema.ports.last";
    public static final String xdm_schema_format_default = "xdm.schema.format.default";
    public static final String xdm_schema_store_type = "xdm.schema.store.type";
    //public static final String xdm_schema_store_class = "xdm.schema.store.class";
    public static final String xdm_schema_store_enabled = "xdm.schema.store.enabled";
    public static final String xdm_schema_store_data_path = "xdm.schema.store.data.path";
    public static final String xdm_schema_store_tx_buffer_size = "xdm.schema.store.tx.buffer.size";
    public static final String xdm_schema_population_size = "xdm.schema.population.size";
    public static final String xdm_schema_population_buffer_size = "xdm.schema.population.buffer.size";
    
    public static final String xdm_document_collections = "xdm.document.collections";
    public static final String xdm_document_data_format = "xdm.document.data.format";
    
}
