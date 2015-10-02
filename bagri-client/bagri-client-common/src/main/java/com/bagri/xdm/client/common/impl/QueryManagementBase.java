package com.bagri.xdm.client.common.impl;

import java.util.Map;

public class QueryManagementBase {

    public int getQueryKey(String query) {
    	// will use cifer hash later..
    	return query.hashCode();
    }
    
	public long getParamsKey(Map<String, Object> params) {
		//final int prime = 31;
		//int result = params.size();
		//for (Map.Entry param: params.entrySet()) {
		//	result = prime * result	+ param.getKey().hashCode();
		//	result = prime * result + param.getValue().hashCode();
		//}
		int result = params.toString().hashCode();
		//logger.trace("getParamsKey; returning key: {} for params: {}", result, params);
		return result;
	}

	
}
