package com.bagri.xdm.domain;

import java.util.Set;

/**
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 * @version 0.5
 */
public abstract class XDMIndexedValue { 

	public XDMIndexedValue() {
		// de-ser
	}

	public abstract int getCount();
	public abstract long getDocumentKey();
	public abstract Set<Long> getDocumentKeys();
	public abstract boolean addDocument(long docKey, long txId);
	public abstract boolean removeDocument(long docKey, long txId);
	public abstract int getSize();

}
