package com.bagri.xdm.api.impl;

import java.util.Map;

import javax.xml.namespace.QName;

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
		//final int prime = 31;
		//int result = params.size();
		//for (Map.Entry param: params.entrySet()) {
		//	result = prime * result	+ param.getKey().hashCode();
		//	result = prime * result + param.getValue().hashCode();
		//}
		if (params == null) {
			return 0;
		}
		int result = params.toString().hashCode();
		//logger.trace("getParamsKey; returning key: {} for params: {}", result, params);
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
