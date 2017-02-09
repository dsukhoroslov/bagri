package com.bagri.xqj;

import javax.xml.xquery.XQException;

public class BagriLogicalXQConnection extends BagriXQConnection {
	
	private BagriPooledXQConnection parent;

	BagriLogicalXQConnection(String username, boolean transactional) {
		super(username, transactional);
	}
	
	void setParent(BagriPooledXQConnection parent) {
		this.parent = parent;
	}

	@Override
	public void close() throws XQException {
		//checkState();
		closeTransaction();
		closed = true;
		// now ask poolead parent to go back to pool... 
		parent.freeConnection();
	}

}
