package com.bagri.xdm.cache.api;

public class XDMCacheConstants {

    /**
     * XDM Document cache
     * Key: DocumentKey; 
     * Value: com.bagri.xdm.XDMDocument
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_DOCUMENT = "xdm-document";

    /**
      * XDM Data cache
      * Key: DataDocumentKey; 
      * Value: com.bagri.xdm.XDMData
      * CacheStore: no
      * Mapped as: no
      */
	public static final String CN_XDM_ELEMENT = "xdm-element";

    /**
     * XDM Index cache
     * Key: PathIndexKey; 
     * Value: com.bagri.xdm.XDMIndexedValue
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_INDEX = "xdm-index";

    /**
     * XDM XML cache
     * Key: Long; documentId 
     * Value: String (XML)
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_CONTENT = "xdm-content";

    /**
     * XDM Query cache
     * Key: Integer; query hash 
     * Value: com.bagri.xdm.XDMQuery
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_QUERY = "xdm-query";

    /**
     * XDM Result cache
     * Key: Long; query/params hash 
     * Value: com.bagri.xdm.XDMResult
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_RESULT = "xdm-result";

    /**
     * XDM Transaction cache
     * Key: Long; transaction identifier 
     * Value: com.bagri.xdm.XDMTransaction
     * CacheStore: yes...
     * Mapped as: no
     */
	public static final String CN_XDM_TRANSACTION = "xdm-transaction";
	
    /**
     * XDM Client cache
     * Key: String; 
     * Value: java.util.Properties
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_CLIENT = "xdm-client";

    /**
     * XDM Namespace cache
     * Key: String (NS URI); Value: com.bagri.xdm.XDMNamespace
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_NAMESPACE_DICT = "dict-namespace";

    /**
     * XDM Path cache
     * Key: String (QName); Value: com.bagri.xdm.XDMPath
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_PATH_DICT = "dict-path";

    /**
     * XDM Index cache
     * Key: String (Index Name); Value: com.bagri.xdm.XDMIndex
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_INDEX_DICT = "dict-index";

    /**
     * XDM Document type cache
     * Key: String; 
     * Value: com.bagri.xdm.XDMDocumentType
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_DOCTYPE_DICT = "dict-document-type";

	// Sequence/IdGen names
    public static final String SQN_DOCUMENT = "xdm.document";
    public static final String SQN_NAMESPACE = "xdm.namespace";
    public static final String SQN_PATH = "xdm.path";
    public static final String SQN_DOCTYPE = "xdm.doctype";
    //public static final String SQN_ELEMENT = "xdm.element";
    public static final String SQN_TRANSACTION = "xdm.transaction";

    // Executor pool names
	public static final String PN_XDM_SCHEMA_POOL = "xdm-exec-pool";
	public static final String PN_XDM_SYSTEM_POOL = "sys-exec-pool";

    // Topic names
	public static final String TPN_XDM_HEALTH = "xdm-health";
	public static final String TPN_XDM_COUNTERS = "xdm-counters";
	
}
