package com.bagri.xdm.cache.api;

import java.util.Map;
import java.util.Set;

import com.bagri.xdm.common.DocumentKey;

/**
 * Helper component which manages distributed population behavior. For cases when there is no a simple mechanism to 
 * construct Document's natural identifier (URI) from the internal Document's key this component manages mapping between 
 * DocumentKeys and URIs.
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface PopulationManagement {

	/**
	 * Returns stored mapping for the key provided
	 * 
	 * @param key the internal Document key 
	 * @return URI of mapping found
	 */
    String getKeyMapping(DocumentKey key);
    
    /**
     * Stores provided key/mapping pair 
     * 
     * @param key the internal Document key 
     * @param mapping the Document uri 
     */
    void setKeyMapping(DocumentKey key, String mapping);
	
	/**
	 * Return mappings between internal DocumentKey structure and Document's URI
	 * 
	 * @param keys the collection of DocumentKey's 
	 * @return mappings for the keys provided
	 */
    Map<DocumentKey, String> getKeyMappings(Set<DocumentKey> keys);

    /**
     * Store mappings provided as a Map
     * 
     * @param mappings the Map of key/uri pairs
     */
    void setKeyMappings(Map<DocumentKey, String> mappings);
	
    /**
     * Deletes mapping identified by key
     * 
     * @param key the internal Document key 
     * @return the URI of deleted mapping
     */
	String deleteKeyMapping(DocumentKey key);
	
	/**
	 * Delete mappings identified by keys
	 * 
	 * @param keys the Collection of internal Document keys  
	 * @return the number of deleted mappings
	 */
	int deleteKeyMappings(Set<DocumentKey> keys);
	
}
