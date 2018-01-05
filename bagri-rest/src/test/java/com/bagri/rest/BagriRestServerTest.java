package com.bagri.rest;

import org.junit.Test;

public class BagriRestServerTest {
	
	@Test
	public void testRestServer() throws Exception {
		//
    	BagriRestServer server = new BagriRestServer();
        try {
            server.start();
            // do something
            Thread.sleep(10000);
        } finally {
            server.stop();
        }
	}

}
