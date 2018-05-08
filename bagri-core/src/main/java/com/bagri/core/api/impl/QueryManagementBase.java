package com.bagri.core.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base implementation for XDM Query Management interface. Several basic methods implemented  
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class QueryManagementBase {

	/**
	 * construct cache key for the query string
	 * 
	 * @param query the query string in XQuery or any other (e.g SQL) syntax
	 * @return cache key for the query
	 */
    public int getQueryKey(String query) {
    	// will use cifer hash later..
    	return query.hashCode();
    }
    
    /**
     * construct cache key for the params Map
     * 
     * @param params the Map&lt;String, Object&gt; of query parameters
     * @return cache key for the params
     */
	public int getParamsKey(Map<String, Object> params) {
		if (params == null) {
			return 0;
		}
		String hash; // = params.toString();
		
		if (params.size() > 1) {
			List<String> keys = new ArrayList<>(params.keySet());
			Collections.sort(keys);
			StringBuilder sb = new StringBuilder("{");
			int idx = 0;
			for (String key: keys) {
				if (idx > 0) {
					sb.append(",");
				}
				Object value = params.get(key);
				sb.append(key).append("=").append(value == null ? "null" : value.toString());
				idx++;
			}
			sb.append("}");
			hash = sb.toString();
		} else {
			hash = params.toString();
		}
		
		int result = hash.hashCode();
		return result;
	}

	/**
	 * construct cache key for the parameterized query
	 * 
	 * @param query the query string
	 * @param params the params Map&lt;String, Object&gt;
	 * @return cache key for the parameterized query
	 */
	public long getResultsKey(String query, Map<String, Object> params) {
		int highKey = getQueryKey(query);
		int lowKey = getParamsKey(params);
		long result = (((long) highKey) << 32) | (lowKey & 0xffffffffL);
		return result;
	}

	/**
	 * split parameterized query key (long) into it parts (int[])
	 * 
	 * @param resultKey the parameterized query key
	 * @return array of parameterized query parts
	 */
	public int[] getResultsKeyParts(long resultKey) {
		return new int[] {(int) resultKey, (int) (resultKey >> 32)};
	}	
	
}
