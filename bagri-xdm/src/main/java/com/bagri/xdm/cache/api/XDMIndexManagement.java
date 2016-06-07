package com.bagri.xdm.cache.api;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.system.XDMIndex;

/**
 * manages schema indices in internal cache, performs translations between indices and pats
 * NOTE: index path can be specified with XPath wildcard expressions
 * 
 * @author Denis Sukhoroslov
 */
public interface XDMIndexManagement {

	/**
	 * check is path indexed or not
	 * 
	 * @param pathId: int; the path id to check
	 * @return true if path already indexed, false otherwise
	 */
	boolean isPathIndexed(int pathId); 
	
	/**
	 * registers a new index
	 * 
	 * @param index: XDMIndex; the new {@link XDMIndex} structure to register in parent XDM Schema
	 * @return an array of registered {@link XDMPath} being indexed 
	 * @throws XDMException in case of any error
	 */
	XDMPath[] createIndex(XDMIndex index) throws XDMException;
	
	/**
	 * removes an existing index

	 * @param index: XDMIndex; the {@link XDMIndex} structure to be removed from the parent XDM Schema
	 * @return an array of registered {@link XDMPath} which were affected by the index
	 * @throws XDMException in case of any error  
	 */
	XDMPath[] dropIndex(XDMIndex index) throws XDMException;

	/**
	 * rebuilds index
	 * NOTE: not implemented yet
	 * 
	 * @param pathId: int; the index path to rebuild
	 * @return true if path was re-indexed, false otherwise 
	 */
	boolean rebuildIndex(int pathId);
	
}
