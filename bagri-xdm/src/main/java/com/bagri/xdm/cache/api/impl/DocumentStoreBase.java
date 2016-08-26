package com.bagri.xdm.cache.api.impl;

import static com.bagri.xdm.common.Constants.ctx_repo;

import java.util.Map;

import com.bagri.xdm.cache.api.SchemaRepository;

/**
 * A common ancestor for future DocumentStore implementations 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class DocumentStoreBase {

    private SchemaRepository xdmRepo;
    private Map<String, Object> userContext;
	
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
	 * Set user context for future use
	 * 
	 * @param context the context
	 */
	protected void setContext(Map<String, Object> context) {
		this.userContext = context;
	}

	
}
