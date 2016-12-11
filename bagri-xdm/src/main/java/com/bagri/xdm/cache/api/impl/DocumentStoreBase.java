package com.bagri.xdm.cache.api.impl;

import static com.bagri.xdm.common.Constants.ctx_repo;
import static com.bagri.xdm.common.Constants.ctx_context;
import static com.bagri.xdm.common.Constants.xdm_schema_store_read_only;

import java.util.Collection;
import java.util.Map;

import com.bagri.xdm.cache.api.SchemaRepository;
import com.bagri.xdm.common.DocumentKey;
import com.bagri.xdm.domain.Document;

/**
 * A common ancestor for future DocumentStore implementations 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class DocumentStoreBase {

    private SchemaRepository xdmRepo;
    private Map<String, Object> userContext;
    protected boolean readOnly = true;
	
	/**
	 * Returns the current schema repository. Can be used after full schema initialization, 
	 * do not use it at init phase; 
	 * 
	 * @return schema repository
	 */
	protected SchemaRepository getRepository() {
		if (xdmRepo == null) {
			xdmRepo = (SchemaRepository) userContext.get(ctx_repo);
		}
		return xdmRepo;
	}
	
	/**
	 * If returns true the store/delete methods will not be invoked at all.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Set user context for future use
	 * 
	 * @param context the context
	 */
	@SuppressWarnings("unchecked")
	protected void setContext(Map<String, Object> context) {
		this.userContext = (Map<String, Object>) context.get(ctx_context);
		String value = (String) context.get(xdm_schema_store_read_only);
		if (value != null) {
			readOnly = value.equalsIgnoreCase("true");
		}
	}

	/**
	 * Stores document to persistent store.
	 * 
	 * @param key the document key
	 * @param value the XDM document instance
	 */
	public void storeDocument(DocumentKey key, Document value) {
		// do nothing
	}

	/**
	 * Stores bunch of documents to persistent store
	 * 
	 * @param entries the map of document keys and corresponding document instances
	 */
	public void storeAllDocuments(Map<DocumentKey, Document> entries) {
		// do nothing
	}

	/**
	 * Deletes document from persistent store
	 * 
	 * @param key the document key
	 */
	public void deleteDocument(DocumentKey key) {
		// do nothing
	}

	/**
	 * Deletes bunch o documents from persistent store 
	 * 
	 * @param keys the keys identifying documents to be deleted 
	 */
	public void deleteAllDocuments(Collection<DocumentKey> keys) {
		// do nothing
	}
	
}
