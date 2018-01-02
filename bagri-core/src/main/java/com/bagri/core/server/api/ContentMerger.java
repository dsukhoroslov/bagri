package com.bagri.core.server.api;

import java.util.Properties;

import com.bagri.core.api.BagriException;

public interface ContentMerger<C> {

    /**
     * Lifecycle method. Invoked at Schema initialization phase. 
     * 
     * @param properties the environment context
     */
    void init(Properties properties);	

	/**
	 * 
	 * @param oldContent old/current document content 
	 * @param newContent new/updated document content
	 * @return merged document content  
	 * @throws BagriException in case of any conversion error
	 */
    C mergeContent(C oldContent, C newContent) throws BagriException;
	
}
