package com.bagri.core.query;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExpressionBuilderTest {

	@Test
	public void testParseQuery() {
	    String query = "/inventory/product-id = $pids";
		ExpressionBuilder eb = new ExpressionBuilder();
		assertTrue(eb.buildExpression(query, 1));
		System.out.println(eb);
	}
	
	@Test
	public void testParseQuery1() {
	    String query = "(/inventory/product-id = $pids) AND ((/inventory/virtual-stores/status = 'active') AND ((/inventory/virtual-stores/region-id = rid) OR (rid = null)))";
		ExpressionBuilder eb = new ExpressionBuilder();
		assertTrue(eb.buildExpression(query, 1));
		System.out.println(eb);
	}
	
	@Test
	public void testParseQuery2() {
	    String query = "((/inventory/virtual-stores/status = 'active') AND ((/inventory/virtual-stores/region-id = rid) OR (rid = null))) AND (/inventory/product-id = $pids)";
		ExpressionBuilder eb = new ExpressionBuilder();
		assertTrue(eb.buildExpression(query, 1));
		System.out.println(eb);
	}

	@Test
	public void testParseQuery3() {
	    String query = "(((/inventory/virtual-stores/region-id = rid) OR (rid = null)) AND (/inventory/virtual-stores/status = 'active')) AND (/inventory/product-id = $pids)";
		ExpressionBuilder eb = new ExpressionBuilder();
		assertTrue(eb.buildExpression(query, 1));
		System.out.println(eb);
	}
}
