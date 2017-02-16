package com.bagri.core.server.api;

/**
 * Cache names and other constants used on client and server sides
 * 
 * @author Denis Sukhoroslov
 *
 */
public class CacheConstants {

	/**
	 * Key mapping cache Key: DocumentKey; Value: String
	 */
	public static final String CN_XDM_KEY = "xdm-key";

	/**
	 * Document cache Key: DocumentKey; Value: com.bagri.core.model.Document
	 */
	public static final String CN_XDM_DOCUMENT = "xdm-document";

	/**
	 * Data cache Key: DataKey; Value: com.bagri.core.model.Elements
	 */
	public static final String CN_XDM_ELEMENT = "xdm-element";

	/**
	 * Index cache Key: PathIndexKey; Value: com.bagri.core.model.IndexedValue
	 */
	public static final String CN_XDM_INDEX = "xdm-index";

	/**
	 * Content cache Key: DocumentKey; Value: String (XML, JSON)
	 */
	public static final String CN_XDM_CONTENT = "xdm-content";

	/**
	 * Query cache Key: Integer, query hash; Value: com.bagri.core.model.Query
	 */
	public static final String CN_XDM_QUERY = "xdm-query";

	/**
	 * Result cache Key: Long, query/params hash; Value: com.bagri.core.model.QueryResult
	 */
	public static final String CN_XDM_RESULT = "xdm-result";

	/**
	 * Transaction cache Key: Long; transaction identifier;
	 * Value: com.bagri.core.model.Transaction
	 */
	public static final String CN_XDM_TRANSACTION = "xdm-transaction";

	/**
	 * Client cache Key: String; Value: java.util.Properties
	 */
	public static final String CN_XDM_CLIENT = "xdm-client";

	/**
	 * Namespace cache Key: String (NS URI); Value: com.bagri.xdm.XDMNamespace
	 */
	public static final String CN_XDM_NAMESPACE_DICT = "dict-namespace";

	/**
	 * Path cache Key: String (QName); Value: com.bagri.core.model.Path
	 */
	public static final String CN_XDM_PATH_DICT = "dict-path";

	/**
	 * Document type cache Key: String; Value: com.bagri.core.model.DocumentType
	 */
	public static final String CN_XDM_DOCTYPE_DICT = "dict-document-type";

	/**
	 * Index cache Key: String (Index Name); Value: com.bagri.core.model.Index
	 */
	public static final String CN_XDM_INDEX_DICT = "dict-index";

	// Sequence/IdGen names

	/**
	 * Document sequence generator
	 */
	public static final String SQN_DOCUMENT = "xdm.document";

	/**
	 * Namespace sequence generator
	 */
	public static final String SQN_NAMESPACE = "xdm.namespace";

	/**
	 * Path sequence generator
	 */
	public static final String SQN_PATH = "xdm.path";

	/**
	 * DocumentType sequence generator
	 */
	public static final String SQN_DOCTYPE = "xdm.doctype";

	/**
	 * Transaction sequence generator
	 */
	public static final String SQN_TRANSACTION = "xdm.transaction";

	// Executor pool names

	/**
	 * Schema cluster execution service
	 */
	public static final String PN_XDM_SCHEMA_POOL = "xdm-exec-pool";

	/**
	 * Schema cluster execution service
	 */
	public static final String PN_XDM_TRANS_POOL = "xdm-trans-pool";

	// Topic names

	/**
	 * Health notifications topic
	 */
	public static final String TPN_XDM_HEALTH = "xdm-health";

	/**
	 * Counters notifications topic
	 */
	public static final String TPN_XDM_COUNTERS = "xdm-counters";

	/**
	 * Population notification topic
	 */
	public static final String TPN_XDM_POPULATION = "xdm-population";
	
	// System/admin distributed objects

	/**
	 * DataFormats cache
	 */
	public static final String CN_SYS_FORMATS = "sys-formats";
	
	/**
	 * DataStores cache
	 */
	public static final String CN_SYS_STORES = "sys-stores";
	
	/**
	 * Modules cache
	 */
	public static final String CN_SYS_MODULES = "sys-modules";
	
	/**
	 * Libraries cache
	 */
	public static final String CN_SYS_LIBRARIES = "sys-libraries";
	
	/**
	 * Nodes cache
	 */
	public static final String CN_SYS_NODES = "sys-nodes";
	
	/**
	 * Schemas cache
	 */
	public static final String CN_SYS_SCHEMAS = "sys-schemas";
	
	/**
	 * Roles cache
	 */
	public static final String CN_SYS_ROLES = "sys-roles";
	
	/**
	 * Users cache
	 */
	public static final String CN_SYS_USERS = "sys-users";
	
	/**
	 * System cluster execution service
	 */
	public static final String PN_XDM_SYSTEM_POOL = "sys-exec-pool";


}
