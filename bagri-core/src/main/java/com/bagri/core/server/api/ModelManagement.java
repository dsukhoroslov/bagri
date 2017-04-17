package com.bagri.core.server.api;

import java.util.Collection;
import java.util.Set;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;

/**
 * XDM document management interface; provided for the client side
 * manages schema meta-data in internal caches;
 * performs translations for namespace, path, document types
 * 
 * @author Denis Sukhoroslov
 */
public interface ModelManagement {
	
	/**
	 * translates full node path like "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
	 * to XDMPath;
	 * 
	 * creates new XDMPath if it is not registered yet;
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param path String; the full node path in Clark form
	 * @param kind XDMNodeKind; the type of the node, one of {@link NodeKind} enum literals
	 * @param dataType int; type of the node value
	 * @param occurrence {@link Occurrence}; multiplicity of the node
	 * @return new or existing {@link Path} structure
	 * @throws BagriException in case of any error
	 */
	Path translatePath(String root, String path, NodeKind kind, int dataType, Occurrence occurrence) throws BagriException;
	
	/**
	 * translates regex expression like "^/ns0:Security/ns0:SecurityInformation/.(*)/ns0:Sector/text\\(\\)$";
	 * to an array of registered pathIds which conforms to the regex specified
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param regex String; regex pattern 
	 * @return Set&lt;Integer&gt;- set of registered pathIds conforming to the pattern provided
	 */
	Set<Integer> translatePathFromRegex(String root, String regex);
	
	/**
	 * translates regex expression like "^/ns0:Security/ns0:SecurityInformation/.(*)/ns0:Sector/text\\(\\)$";
	 * to Collection of registered path which conforms to the regex specified
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param regex String; regex pattern 
	 * @return Set&lt;String&gt;- set of registered paths conforming to the pattern provided
	 */
	Collection<String> getPathFromRegex(String root, String regex);
	
	/**
	 * return array of pathIds which are children of the root specified;
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param root String; root node path 
	 * @return Set&lt;Integer&gt;- set of registered pathIds who are direct or indirect children of the parent path provided
	 */
	Set<Integer> getPathElements(String root);
	
	/**
	 * search for registered full node path like "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
	 * 
	 * @param path String; node path in Clark form
	 * @return registered {@link Path} structure if any
	 */
	Path getPath(String root, String path); 

	/**
	 * return XDM path instance by pathId provided;
	 * 
	 * @param pathId int; the id of registered node path
	 * @return registered {@link Path} structure for the id provided
	 */
	Path getPath(int pathId);
	
	/**
	 * return collection of paths registered for the document type provided;
	 * 
	 * @param typeId int; the corresponding document's type
	 * @return Collection of {@link Path} belonging to the typeId provided; result is sorted by pathId
	 */
	Collection<Path> getTypePaths(String root);
	
	/**
	 * 
	 * @param path the long element path
	 * @return the path root
	 */
	String getPathRoot(String path);
	
	/**
	 * updates the Path in cache
	 * 
	 * @param path the Path to update
	 */
	void updatePath(Path path);

	/**
	 * registers bunch of node path's specified in the XML schema (XSD)   
	 * 
	 * @param schema String; schema in plain text  
	 * @throws BagriException in case of any error
	 */
	void registerSchema(String schema) throws BagriException;
	
	/**
	 * registers bunch of schemas located in the schemaUri folder   
	 * 
	 * @param schemaUri String; the folder containing schemas to register  
	 * @throws BagriException in case of any error
	 */
	void registerSchemaUri(String schemaUri) throws BagriException;
	
}
