package com.bagri.xdm.domain;

import static com.bagri.common.util.FileUtils.def_encoding;

import java.util.Date;
import java.util.List;

public class XDMFragmentedDocument extends XDMDocument {

	private long[] fragments;
	
	public XDMFragmentedDocument() {
		super();
	}
	
	public XDMFragmentedDocument(long documentId, String uri, int typeId, String owner, long txId, int bytes, int elts) {
		this(documentId, 0, uri, typeId, txId, 0, new Date(), owner, def_encoding, bytes, elts);
	}
	
	public XDMFragmentedDocument(long documentId, int version, String uri, int typeId, String owner, long txId, int bytes, int elts) {
		this(documentId, version, uri, typeId, txId, 0, new Date(), owner, def_encoding, bytes, elts);
	}

	public XDMFragmentedDocument(long documentId, int version, String uri, int typeId, long txStart, long txFinish, Date createdAt, 
			String createdBy, String encoding, int bytes, int elts) {
		super(documentId, version, uri, typeId, txStart, txFinish, createdAt, createdBy, encoding, bytes, elts);
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
