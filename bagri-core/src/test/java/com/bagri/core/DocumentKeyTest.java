package com.bagri.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.bagri.core.DocumentKey;

public class DocumentKeyTest {
	
	@Test
	public void testDocumentKey() {
		
		DocumentKey key = new DocumentKey(1, 0, 0);
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
					DocumentKey key = new DocumentKey(l, k, i);
					assertEquals(key.getHash(), l);
					assertEquals(key.getRevision(), k);
					assertEquals(key.getVersion(), i);
				}
			}
		}
	}

}
