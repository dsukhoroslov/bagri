package com.bagri.xdm.cache.api;

/**
 * Cache names and other constants used on client and server sides
 * 
 * @author Denis Sukhoroslov
 *
 */
public class CacheConstants {

    /**
     * XDM Key mapping cache
     * Key: DocumentKey; 
     * Value: String
     */
	public static final String CN_XDM_KEY = "xdm-key";

	/**
     * XDM Document cache
     * Key: DocumentKey; 
     * Value: com.bagri.xdm.domain.Document
     */
	public static final String CN_XDM_DOCUMENT = "xdm-document";

    /**
      * XDM Data cache
      * Key: DataKey; 
      * Value: com.bagri.xdm.XDMElements
      */
	public static final String CN_XDM_ELEMENT = "xdm-element";

    /**
     * XDM Index cache
     * Key: PathIndexKey; 
     * Value: com.bagri.xdm.domain.IndexedValue
     */
	public static final String CN_XDM_INDEX = "xdm-index";

    /**
     * XDM Content cache
     * Key: DocumentKey; 
     * Value: String (XML, JSON)
     */
	public static final String CN_XDM_CONTENT = "xdm-content";

    /**
     * XDM Query cache
     * Key: Integer; query hash 
     * Value: com.bagri.xdm.XDMQuery
     */
	public static final String CN_XDM_QUERY = "xdm-query";

    /**
     * XDM Result cache
     * Key: Long; query/params hash 
     * Value: com.bagri.xdm.XDMResult
     */
	public static final String CN_XDM_RESULT = "xdm-result";

    /**
     * XDM Transaction cache
     * Key: Long; transaction identifier 
     * Value: com.bagri.xdm.XDMTransaction
     */
	public static final String CN_XDM_TRANSACTION = "xdm-transaction";
	
    /**
     * XDM Client cache
     * Key: String; 
     * Value: java.util.Properties
     */
	public static final String CN_XDM_CLIENT = "xdm-client";

    /**
     * XDM Namespace cache
     * Key: String (NS URI); Value: com.bagri.xdm.XDMNamespace
     */
	public static final String CN_XDM_NAMESPACE_DICT = "dict-namespace";

    /**
     * XDM Path cache
     * Key: String (QName); 
     * Value: com.bagri.xdm.XDMPath
     */
	public static final String CN_XDM_PATH_DICT = "dict-path";

    /**
     * XDM Document type cache
     * Key: String; 
     * Value: com.bagri.xdm.XDMDocumentType
     */
	public static final String CN_XDM_DOCTYPE_DICT = "dict-document-type";

    /**
     * XDM Index cache
     * Key: String (Index Name); 
     * Value: com.bagri.xdm.XDMIndex
     */
	public static final String CN_XDM_INDEX_DICT = "dict-index";

	// Sequence/IdGen names
	
	/**
	 * XDMDocument sequence generator
	 */
    public static final String SQN_DOCUMENT = "xdm.document";
    
    /**
     * XDMNamespace sequenec generator
     */
    public static final String SQN_NAMESPACE = "xdm.namespace";
    
    /**
     * XDMPath sequence generator
     */
    public static final String SQN_PATH = "xdm.path";
    
    /**
     * XDMDocumentType sequence generator
     */
    public static final String SQN_DOCTYPE = "xdm.doctype";
    
    /**
     * XDMTransaction sequence generator
     */
    public static final String SQN_TRANSACTION = "xdm.transaction";

    // Executor pool names
    
    /**
     * XDM Schema cluster execution service
     */
	public static final String PN_XDM_SCHEMA_POOL = "xdm-exec-pool";
	
	/**
	 * XDM System cluster execution service
	 */
	public static final String PN_XDM_SYSTEM_POOL = "sys-exec-pool";

    // Topic names
	
	/**
	 * XDM Health notifications topic
	 */
	public static final String TPN_XDM_HEALTH = "xdm-health";
	
	/**
	 * XDM Counters notifications topic
	 */
	public static final String TPN_XDM_COUNTERS = "xdm-counters";
	
}
