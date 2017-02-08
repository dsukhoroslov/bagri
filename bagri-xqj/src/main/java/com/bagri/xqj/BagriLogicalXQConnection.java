package com.bagri.xqj;

import javax.xml.xquery.XQException;

public class BagriLogicalXQConnection extends BagriXQConnection {

	public BagriLogicalXQConnection(String username, boolean transactional) {
		super(username, transactional);
	}

	@Override
	public void close() throws XQException {
		//checkState();
		closeTransaction();
		closed = true;
		// now ask poolead parent to go back to pool... 
		//logger.debug("close.");
	}

}
