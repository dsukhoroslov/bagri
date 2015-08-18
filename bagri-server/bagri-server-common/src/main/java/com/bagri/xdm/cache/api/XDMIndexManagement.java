package com.bagri.xdm.cache.api;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.system.XDMIndex;

public interface XDMIndexManagement {

	/**
	 * @param pathId
	 * check is path indexed or not
	 * 
	 */
	boolean isPathIndexed(int pathId); 
	
	/**
	 * @param typeId
	 * registers a new index
	 * @throws XDMException 
	 * 
	 */
	XDMPath[] createIndex(XDMIndex index) throws XDMException;
	
	/**
	 * @param typeId
	 * remove an existing index
	 * @throws XDMException 
	 * 
	 */
	XDMPath[] deleteIndex(XDMIndex index) throws XDMException;


	boolean rebuildIndex(int pathId);
	
}
