package com.bagri.xdm.domain;

import static com.bagri.common.util.FileUtils.def_encoding;
import static com.bagri.xdm.common.XDMDocumentKey.toHash;
import static com.bagri.xdm.common.XDMDocumentKey.toRevision;
import static com.bagri.xdm.common.XDMDocumentKey.toVersion;

import java.util.Date;

public class XDMFragmentedDocument extends XDMDocument {

	private long[] fragments;
	
	public XDMFragmentedDocument() {
		super();
	}
	
	public XDMFragmentedDocument(long docKey, String uri, int typeId, String owner, long txId, int bytes, int elts) {
		this(docKey, uri, typeId, txId, 0, new Date(), owner, def_encoding, bytes, elts);
	}

	public XDMFragmentedDocument(long docKey, String uri, int typeId, long txStart, long txFinish, Date createdAt, 
			String createdBy, String encoding, int bytes, int elts) {
		super(docKey, uri, typeId, txStart, txFinish, createdAt, createdBy, encoding, bytes, elts);
	}
	
	public long[] getFragments() {
		return fragments;
	}
	
	public void setFragments(long[] fragments) {
		long first = fragments[0];
		if (first == getDocumentKey()) {
			this.fragments = fragments;
		} else {
			this.fragments = new long[fragments.length + 1];
			this.fragments[0] = getDocumentKey();
			System.arraycopy(fragments, 0, this.fragments, 1, fragments.length - 1);
		}
	}
	
}
