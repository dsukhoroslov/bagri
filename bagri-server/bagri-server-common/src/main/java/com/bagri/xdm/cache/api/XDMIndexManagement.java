package com.bagri.xdm.cache.api;

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
	 * 
	 */
	XDMPath[] createIndex(XDMIndex index);
	
	/**
	 * @param typeId
	 * remove an existing index
	 * 
	 */
	XDMPath[] deleteIndex(XDMIndex index);


	boolean rebuildIndex(int pathId);
	
}
