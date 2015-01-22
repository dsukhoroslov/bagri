package com.bagri.xdm.access.api;

public class XDMCacheConstants {

    /**
     * XDM Document cache
     * Key: Long; Value: com.bagri.xdm.XDMDocument
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_DOCUMENT = "xdm-document";

    /**
      * XDM Data cache
      * Key: DataDocumentKey; Value: com.bagri.xdm.XDMData
      * CacheStore: no
      * Mapped as: no
      */
	public static final String CN_XDM_ELEMENT = "xdm-element";

    /**
     * XDM Path cache
     * Key: PathIndexKey; Value: com.bagri.xdm.XDMIndex
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_INDEX = "xdm-index";

    /**
     * XDM Path-Value cache
     * Key: PathValueDocumentKey; Value: com.bagri.xdm.XDMIndex<String>
     * CacheStore: no
     * Mapped as: no
     */
	//public static final String CN_XDM_PATH_VALUE_INDEX = "xdm-path-value-index";
	
    /**
     * XDM XML cache
     * Key: Long; documentId 
     * Value: String (XML)
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_XML = "xdm-xml";

    /**
     * XDM Query cache
     * Key: Long; query hash 
     * Value: com.bagri.xdm.XDMQuery
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_QUERY = "xdm-query";

    /**
     * XDM Result cache
     * Key: Long; query hash 
     * Value: com.bagri.xdm.XDMResult
     * CacheStore: no
     * Mapped as: no
     */
	public static final String CN_XDM_RESULT = "xdm-result";

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
     * XDM Document cache
     * Key: Long; Value: com.bagri.xdm.XDMDocument
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
    
	public static final String PN_XDM_SCHEMA_POOL = "xdm-exec-pool";
	public static final String PN_XDM_SYSTEM_POOL = "sys-exec-pool";
}
