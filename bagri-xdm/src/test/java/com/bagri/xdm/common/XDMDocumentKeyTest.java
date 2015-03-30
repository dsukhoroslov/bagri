package com.bagri.xdm.common;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class XDMDocumentKeyTest {
	
	@Test
	public void testDocumentKey() {
		
		XDMDocumentKey key = new XDMDocumentKey(1, 0);
		System.out.println(key);
		assertTrue(key.getDocumentId() == 1L);
		assertTrue(key.getVersion() == 0);
	}
	
	@Test
	public void testDocumentKeys() {
		
		// takes too much time
		//for (long l = 1; l < Integer.MAX_VALUE; l++) {
		for (long l = 1; l < 1000000; l++) {
			for (int i=1; i < 0xFF; i++) {
				XDMDocumentKey key = new XDMDocumentKey(l, i);
				assertTrue(key.getDocumentId() == l);
				assertTrue(key.getVersion() == i);
			}
		}
	}

}
