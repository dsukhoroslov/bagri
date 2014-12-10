package com.bagri.xdm.access.api;

import java.util.Collection;
import java.util.Set;

import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;

/**
 * manages schema meta-data in internal replicated caches;
 * performs translations for namespace, path, document types
 * 
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 */
public interface XDMSchemaDictionary {
	
	/**
	 * performs translation from full namespace declaration to its prefix part:
	 * http://tpox-benchmark.com/security -> ns0
	 * 
	 * creates new prefix in case when new (not yet registered) namespace provided;
	 */
	String getNamespacePrefix(String namespace);
	
	/**
	 * 
	 * @param namespace
	 * @return namespace prefix: http://tpox-benchmark.com/security -> ns0
	 */
	String translateNamespace(String namespace);
	
	/**
	 * 
	 * @param namespace
	 * @param prefix
	 * @return namespace prefix: http://tpox-benchmark.com/security -> ns0
	 */
	String translateNamespace(String namespace, String prefix);

	/**
	 * translates full node path like "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name"
	 * to its prefixed equivalent: "/ns0:Security/ns0:Name" 
	 */
	String normalizePath(String path);
	
	/**
	 * translates full node path like "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
	 * to int pathId;
	 * 
	 * registers new pathId if it is not registered yet;  
	 */
	XDMPath translatePath(int typeId, String path, XDMNodeKind kind);
	
	/**
	 * translates regex expression like "^/ns0:Security/ns0:SecurityInformation/.(*)/ns0:Sector/text\\(\\)$";
	 * to an array of registered pathIs which conforms to the regex specified
	 */
	Set<Integer> translatePathFromRegex(int typeId, String regex);
	
	/**
	 * translates regex expression like "^/ns0:Security/ns0:SecurityInformation/.(*)/ns0:Sector/text\\(\\)$";
	 * to Collection of registered path which conforms to the regex specified
	 */
	Collection<String> getPathFromRegex(int typeId, String regex);
	
	/**
	 * return array of pathIds which are children of the root specified;
	 * 
	 */
	Set<Integer> getPathElements(int typeId, String root);
	
	/**
	 * return XDM path instance by pathId provided;
	 * 
	 */
	XDMPath getPath(int pathId);
	
	/**
	 * 
	 * @param typeId
	 * @return Collection of XDMPath belonging to the typeId provided.
	 * result is sorted by pathId
	 */
	Collection<XDMPath> getTypePaths(int typeId);
	
	/**
	 * returns document type ID for the root document element specified. root is a long
	 * path representation: "/{http://tpox-benchmark.com/security}Security"
	 * 
	 * returns -1 in case when root path is not registered in docType cache yet;
	 */
	int getDocumentType(String root);
	
	/**
	 * the opposite method to getDocumentType;
	 */
	String getDocumentRoot(int typeId);
	
	/**
	 * works like getDocumentType(String root), but registers a new document type
	 * in case when it is not registered yet.
	 */
	int translateDocumentType(String root);
	
	/**
	 * 
	 * @param typeId
	 */
	void normalizeDocumentType(int typeId);

	void registerSchema(String schema);
	void registerSchemaUri(String schemaUri);
	
}
