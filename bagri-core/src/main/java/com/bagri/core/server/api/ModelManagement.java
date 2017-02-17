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
	 * performs translation from full namespace declaration to its prefix part:
	 * http://tpox-benchmark.com/security -&gt; ns0.
	 * 
	 * creates new prefix in case when new (not registered yet) namespace provided;
	 * 
	 * @param namespace String; the full namespace declaration 
	 * @return namespace prefix: String; ns0
	 */
	String getNamespacePrefix(String namespace);
	
	/**
	 * performs translation from full namespace declaration to its prefix part:
	 * http://tpox-benchmark.com/security -&gt; ns0
	 * 
	 * returns null in case when new (not registered yet) namespace provided;
	 * 
	 * @param namespace String; the full namespace declaration
	 * @return namespace prefix: String; ns0 or null
	 */
	String translateNamespace(String namespace);
	
	/**
	 * performs translation from full namespace declaration to its prefix part:
	 * http://tpox-benchmark.com/security -&gt; ns0
	 * 
	 * creates new prefix in case when new (not registered yet) namespace provided;
	 * uses the suggested prefix for the new one
	 * 
	 * @param namespace String; the full namespace declaration
	 * @param prefix String; the prefix suggested to use when the namespace is not registered yet, e.g. xsi
	 * @return namespace prefix: String; xsi
	 */
	String translateNamespace(String namespace, String prefix);

	/**
	 * translates full node path like "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name"
	 * to its prefixed equivalent: "/ns0:Security/ns0:Name"
	 *  
	 * @param path String; the full node path in Clark form
	 * @return normalized path: STring; e.g. "/ns0:Security/ns0:Name"
	 */
	String normalizePath(String path);
	
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
	Path translatePath(int typeId, String path, NodeKind kind, int dataType, Occurrence occurrence) throws BagriException;
	
	/**
	 * translates regex expression like "^/ns0:Security/ns0:SecurityInformation/.(*)/ns0:Sector/text\\(\\)$";
	 * to an array of registered pathIds which conforms to the regex specified
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param regex String; regex pattern 
	 * @return Set&lt;Integer&gt;- set of registered pathIds conforming to the pattern provided
	 */
	Set<Integer> translatePathFromRegex(int typeId, String regex);
	
	/**
	 * translates regex expression like "^/ns0:Security/ns0:SecurityInformation/.(*)/ns0:Sector/text\\(\\)$";
	 * to Collection of registered path which conforms to the regex specified
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param regex String; regex pattern 
	 * @return Set&lt;String&gt;- set of registered paths conforming to the pattern provided
	 */
	Collection<String> getPathFromRegex(int typeId, String regex);
	
	/**
	 * return array of pathIds which are children of the root specified;
	 * 
	 * @param typeId int; the corresponding document's type
	 * @param root String; root node path 
	 * @return Set&lt;Integer&gt;- set of registered pathIds who are direct or indirect children of the parent path provided
	 */
	Set<Integer> getPathElements(int typeId, String root);
	
	/**
	 * search for registered full node path like "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
	 * 
	 * @param path String; node path in Clark form
	 * @return registered {@link Path} structure if any
	 */
	Path getPath(String path); 

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
	Collection<Path> getTypePaths(int typeId);
	
	/**
	 * returns document type ID for the root document element specified. root is a long
	 * path representation like {@literal "/{http://tpox-benchmark.com/security}Security"}
	 * 
	 * returns -1 in case when root path is not registered in docType cache yet;
	 * 
	 * @param root String; the document root path
	 * @return document typeId registered for the root path  
	 */
	int getDocumentType(String root);
	
	/**
	 * returns document root path like {@literal "/{http://tpox-benchmark.com/security}Security"}
	 * for the typeId specified
	 * 
	 * @param typeId int; the document's type id
	 * @return String path registered for the type id  
	 */
	String getDocumentRoot(int typeId);
	
	/**
	 * returns document type ID for the root document element specified. root is a long
	 * path representation like {@literal "/{http://tpox-benchmark.com/security}Security"}
	 * 
	 * returns new typeId in case when root path is not registered in docType cache yet;
	 * 
	 * @param root String; the document root path
	 * @return int document typeId registered for the root path  
	 */
	int translateDocumentType(String root);
	
	/**
	 * normalizes all registered paths belonging to the document type id. 
	 * i.e. set their parentId and pathId attributes properly  
	 * 
	 * @param typeId int; the document's type id  
	 * @throws BagriException in case of any error 
	 */
	//void normalizeDocumentType(int typeId) throws BagriException;

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
