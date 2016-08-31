package com.bagri.xdm.cache.api;

import java.util.Map;
import java.util.Set;

import com.bagri.xdm.common.DocumentKey;

/**
 * Helper component which manages distributed population behavior
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface PopulationManagement {

	/**
	 * 
	 * @param key
	 * @return
	 */
    String getKeyMapping(DocumentKey key);
    
    /**
     * 
     * @param key
     * @param mapping
     */
    void setKeyMapping(DocumentKey key, String mapping);
	
	/**
	 * provide mappings between internal DocumentKey structure and Document's URI
	 * 
	 * @param keys DocumentKey's 
	 * @return mappings for the keys provided
	 */
    Map<DocumentKey, String> getKeyMappings(Set<DocumentKey> keys);

    /**
     * store mappings
     * 
     * @param mappings
     */
    void setKeyMappings(Map<DocumentKey, String> mappings);
	
    /**
     * 
     * @param key
     * @return
     */
	String deleteKeyMapping(DocumentKey key);
	
	/**
	 * 
	 * @param keys
	 * @return
	 */
	int deleteKeyMappings(Set<DocumentKey> keys);
	
}
