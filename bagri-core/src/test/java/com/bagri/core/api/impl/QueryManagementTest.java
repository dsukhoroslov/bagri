package com.bagri.core.api.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.QueryManagement;
import com.bagri.core.api.ResultCursor;

public class QueryManagementTest {
	
	//@Test
	//public void testQueryKey() throws Exception {
	//	QueryManagementImpl qm = new QueryManagementImpl();
	//	qm.getQueryKey(query)
	//}
	
	@Test
	public void testResultKey() throws Exception {
		QueryManagementImpl qm = new QueryManagementImpl();
		//String query = "this is a dummy query";
		Map<String, Object> params = new HashMap<>();
		params.put("param1", 123);
		params.put("param2", "ABCDE");
		List list = new ArrayList();
		params.put("p3", list);
		int key1 = qm.getParamsKey(params);
		params.remove("param1");
		params.put("param1", 123);
		int key2 = qm.getParamsKey(params);
		assertEquals(key1, key2);
		params.remove("p3");
		params.put("p3", list);
		int key3 = qm.getParamsKey(params);
		assertEquals(key2, key3);
	}
	
	private static class QueryManagementImpl extends QueryManagementBase implements QueryManagement {

		@Override
		public <T> ResultCursor<T> executeQuery(String query, Map<String, Object> params, Properties props)
				throws BagriException {
			return null;
		}

		@Override
		public ResultCursor<String> getDocumentUris(String query, Map<String, Object> params, Properties props)
				throws BagriException {
			return null;
		}

		@Override
		public void cancelExecution() throws BagriException {
		}

		@Override
		public Collection<String> prepareQuery(String query) {
			return null;
		}
		
	}
}
