package com.bagri.xdm.cache.api;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.Path;
import com.bagri.xdm.system.Index;

/**
 * manages schema indices in internal cache, performs translations between indices and pats
 * NOTE: index path can be specified with XPath wildcard expressions
 * 
 * @author Denis Sukhoroslov
 */
public interface IndexManagement {

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
	 * @param index: XDMIndex; the new {@link Index} structure to register in parent XDM Schema
	 * @return an array of registered {@link Path} being indexed 
	 * @throws XDMException in case of any error
	 */
	Path[] createIndex(Index index) throws XDMException;
	
	/**
	 * removes an existing index

	 * @param index: XDMIndex; the {@link Index} structure to be removed from the parent XDM Schema
	 * @return an array of registered {@link Path} which were affected by the index
	 * @throws XDMException in case of any error  
	 */
	Path[] dropIndex(Index index) throws XDMException;

	/**
	 * rebuilds index
	 * NOTE: not implemented yet
	 * 
	 * @param pathId: int; the index path to rebuild
	 * @return true if path was re-indexed, false otherwise 
	 */
	boolean rebuildIndex(int pathId);
	
}
