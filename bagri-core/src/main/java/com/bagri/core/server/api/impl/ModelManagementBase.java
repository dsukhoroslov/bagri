package com.bagri.core.server.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQItemType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.support.idgen.IdGenerator;

/**
 * Base implementation for XDM Model Management interface. Very close to its client ancestor class. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class ModelManagementBase implements ModelManagement {
	
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    protected static final long timeout = 100; // 100ms to wait for lock..

	protected abstract Map<String, Path> getPathCache();
	protected abstract IdGenerator<Long> getPathGen();
    
	//protected abstract <K> boolean lock(Map<K, ?> cache, K key); 
	//protected abstract <K> void unlock(Map<K, ?> cache, K key); 
	protected abstract <K, V> V putIfAbsent(Map<K, V> cache, K key, V value);
	//protected abstract <K, V> V putPathIfAbsent(Map<K, V> cache, K key, V value);

	protected abstract Set<Map.Entry<String, Path>> getTypedPathEntries(String root);
	protected abstract Set<Map.Entry<String, Path>> getTypedPathWithRegex(String regex, String root);

	/**
	 * WRONG_PATH identifies path not existing in XDMPath dictionary
	 */
    public static final int WRONG_PATH = -1;

	protected String getPathKey(String root, String path) {
		return root + ":" + path;
	}

	/**
	 * search for registered full node path like "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
	 * 
	 * @param path String; node path in Clark form
	 * @return registered {@link Path} structure if any
	 */
	public Path getPath(String root, String path) {
		String pathKey = getPathKey(root, path);
		return getPathCache().get(pathKey);
	}
    
	/**
	 * translates full node path like "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
	 * to XDMPath;
	 * 
	 * creates new XDMPath if it is not registered yet;
	 * 
	 * @param root String; the corresponding document's root
	 * @param path String; the full node path in Clark form
	 * @param kind XDMNodeKind; the type of the node, one of {@link NodeKind} enum literals
	 * @param dataType int; type of the node value
	 * @param occurrence {@link Occurrence}; multiplicity of the node
	 * @return new or existing {@link Path} structure
	 * @throws BagriException in case of any error
	 */
	public Path translatePath(String root, String path, NodeKind kind, int parentId, int dataType, Occurrence occurrence) throws BagriException {
		// "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Name/text()"
		
		//if (kind != NodeKind.document) {
			if (path == null || path.length() == 0) {
				return null; //WRONG_PATH;
			}
		
			//path = normalizePath(path);
		//}
		Path result = addDictionaryPath(root, path, kind, parentId, dataType, occurrence); 
		return result;
	}
	
	/**
	 * 
	 * @param path the long element path
	 * @return the path root
	 */
	public String getPathRoot(String path) {
		if (path.startsWith("/{")) {
			int pos = path.indexOf("/", path.indexOf("}"));
			if (pos > 0) {
				return path.substring(0, path.indexOf("/", path.indexOf("}")));
			}
			return path;
		}
		String[] segments = path.split("/");
		if (segments.length > 1) {
			return "/" + segments[1];
		} else if (segments.length > 0) {
			return "/" + segments[0];
		}
		return null;
	}
	
	/**
	 * return array of pathIds which are children of the root specified;
	 * 
	 * @param root String; root node path 
	 * @return Set&lt;Integer&gt;- set of registered pathIds who are direct or indirect children of the parent path provided
	 */
	public Set<Integer> getPathElements(String root) {

		logger.trace("getPathElements.enter; got root: {}", root);
		Set<Integer> result = new HashSet<Integer>();
		String pathKey = getPathKey(getPathRoot(root), root);
		Path xPath = getPathCache().get(pathKey);
		if (xPath != null) {
			int pId = xPath.getPathId();
			while (pId <= xPath.getPostId()) {
				result.add(pId);
				pId++;
			}
		}
		logger.trace("getPathElements.exit; returning: {}", result);
		return result; 
	}

	protected Path addDictionaryPath(String root, String path, NodeKind kind, int parentId, int dataType, Occurrence occurrence) throws BagriException {

		String pathKey = getPathKey(root, path);
		Path xpath = getPathCache().get(pathKey);
		if (xpath == null) {
			int pathId = getPathGen().next().intValue();
			int postId = 0;
			if (kind == NodeKind.attribute || kind == NodeKind.comment || kind == NodeKind.namespace ||
					kind == NodeKind.pi || kind == NodeKind.text) {
				postId = pathId;
			}
			xpath = new Path(path, root, kind, pathId, parentId, postId, dataType, occurrence); 
			xpath = putIfAbsent(getPathCache(), pathKey, xpath);
		}
		if (parentId > 0 && xpath.getParentId() != parentId) {
			xpath.setParentId(parentId);
			updatePath(xpath);
		}
		return xpath;
	}
	
	public void updatePath(Path path) {
		String pathKey = getPathKey(path.getRoot(), path.getPath());
		getPathCache().put(pathKey, path);
	}

	/**
	 * translates regex expression like "^/ns0:Security/ns0:SecurityInformation/.(*)/ns0:Sector/text\\(\\)$";
	 * to an array of registered pathIds which conforms to the regex specified
	 * 
	 * @param root String; the corresponding document's root
	 * @param regex String; regex pattern 
	 * @return Set&lt;Integer&gt;- set of registered pathIds conforming to the pattern provided
	 */
	public Set<Integer> translatePathFromRegex(String root, String regex) {

		logger.trace("translatePathFromRegex.enter; got regex: {}, root: {}", regex, root);
		Set<Map.Entry<String, Path>> entries = getTypedPathWithRegex(regex, root); 
		Set<Integer> result = new HashSet<Integer>(entries.size());
		for (Map.Entry<String, Path> e: entries) {
			logger.trace("translatePathFromRegex; path found: {}", e.getValue());
			result.add(e.getValue().getPathId());
		}

		logger.trace("translatePathFromRegex.exit; returning: {}", result);
		return result;
	}

	/**
	 * translates regex expression like "^/ns0:Security/ns0:SecurityInformation/.(*)/ns0:Sector/text\\(\\)$";
	 * to Collection of registered path which conforms to the regex specified
	 * 
	 * @param root String; the corresponding document's type
	 * @param regex String; regex pattern 
	 * @return Set&lt;String&gt;- set of registered paths conforming to the pattern provided
	 */
	public Collection<String> getPathFromRegex(String root, String regex) {
		logger.trace("getPathFromRegex.enter; got regex: {}, root: {}", regex, root);
		Set<Map.Entry<String, Path>> entries = getTypedPathWithRegex(regex, root); 
		List<String> result = new ArrayList<String>(entries.size());
		for (Map.Entry<String, Path> e: entries) {
			logger.trace("getPathFromRegex; path found: {}", e.getValue());
			result.add(e.getKey());
		}
		logger.trace("getPathFromRegex.exit; returning: {}", result);
		return result;
	}
	
	//protected int[] fromCollection(Collection<Integer> from) {
	//	int idx = 0;
	//	int[] result = new int[from.size()];
	//	for (Integer i: from) {
	//		result[idx++] = i;
	//	}
	//	return result;
	//}

}
