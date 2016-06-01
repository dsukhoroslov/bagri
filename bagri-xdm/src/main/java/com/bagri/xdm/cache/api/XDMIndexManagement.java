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
	 * @param index
	 * registers a new index
	 * @throws XDMException 
	 * 
	 */
	XDMPath[] createIndex(XDMIndex index) throws XDMException;
	
	/**
	 * @param index
	 * removes an existing index
	 * @throws XDMException 
	 * 
	 */
	XDMPath[] dropIndex(XDMIndex index) throws XDMException;

	/**
	 * @param pathId
	 * rebuilds index
	 * 
	 */
	boolean rebuildIndex(int pathId);
	
}
