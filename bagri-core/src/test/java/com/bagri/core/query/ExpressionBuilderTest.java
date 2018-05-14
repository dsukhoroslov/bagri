package com.bagri.core.query;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExpressionBuilderTest {

	@Test
	public void testParseQuery() {
	    String query = "(/inventory/product-id = $pids) AND ((/inventory/virtual-stores/status = 'active') AND ((/inventory/virtual-stores/region-id = rid) OR (rid = null)))";
		ExpressionBuilder eb = new ExpressionBuilder();
		assertTrue(eb.buildExpression(query));
		System.out.println(eb.getExpressions());
	}
	
}
