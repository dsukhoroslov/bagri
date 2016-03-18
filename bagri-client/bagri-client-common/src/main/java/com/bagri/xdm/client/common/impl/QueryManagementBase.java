package com.bagri.xdm.client.common.impl;

import java.util.Map;

import javax.xml.namespace.QName;

public class QueryManagementBase {

    public int getQueryKey(String query) {
    	// will use cifer hash later..
    	return query.hashCode();
    }
    
	public int getParamsKey(Map<QName, Object> params) {
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

	public long getResultsKey(String query, Map<QName, Object> params) {
		int highKey = getQueryKey(query);
		int lowKey = getParamsKey(params);
		long result = (((long) highKey) << 32) | (lowKey & 0xffffffffL);
		return result;
	}

	public int[] getResultsKeyParts(long resultKey) {
		return new int[] {(int) resultKey, (int) (resultKey >> 32)};
	}	
}
