package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.HashSet;
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
	public abstract long getDocumentId();
	public abstract Set<Long> getDocumentIds();
	public abstract boolean addDocument(long docId, long txId);
	public abstract boolean removeDocument(long docId, long txId);
	public abstract int getSize();

}
