package com.bagri.server.hazelcast.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.bagri.core.DataKey;
import com.bagri.core.DocumentKey;
import com.bagri.core.IndexKey;
import com.bagri.core.KeyFactory;

public class KeyFactoryTest {
	
	private KeyFactory factory;
	
	@Before
	public void setUp() throws Exception {
		factory = new KeyFactoryImpl();
	}	
	
	@Test
	public void testDocumentTests() {
		DocumentKey dk1 = factory.newDocumentKey("user5916344658280979961", 0, 1);
		DocumentKey dk2 = factory.newDocumentKey("user9077967451922481423", 0, 1);
		assertEquals(dk1, dk2);
	}

}
