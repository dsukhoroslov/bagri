package com.bagri.xdm.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class XDMDocumentKeyTest {
	
	@Test
	public void testDocumentKey() {
		
		XDMDocumentKey key = new XDMDocumentKey(1, 0, 0);
		System.out.println(key);
		assertEquals(key.getHash(), 1L);
		assertEquals(key.getRevision(), 0);
		assertEquals(key.getVersion(), 0);
	}
	
	@Test
	public void testDocumentKeys() {
		
		// takes too much time
		//for (long l = 1; l < Integer.MAX_VALUE; l++) {
		for (int l = 1; l < 10000; l++) {
			for (int k=1; k < 0xFF; k++) {
				for (int i=1; i < 0xFF; i++) {
					XDMDocumentKey key = new XDMDocumentKey(l, k, i);
					assertEquals(key.getHash(), l);
					assertEquals(key.getRevision(), k);
					assertEquals(key.getVersion(), i);
				}
			}
		}
	}

}
