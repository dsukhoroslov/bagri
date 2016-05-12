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
	
	public XDMFragmentedDocument(String uri, int typeId, String owner, long txId, int bytes, int elts) {
		this(uri.hashCode(), 0, dvFirst, uri, typeId, txId, 0, new Date(), owner, def_encoding, bytes, elts);
	}
	
	public XDMFragmentedDocument(int hash, int revision, int version, String uri, int typeId, String owner, long txId, int bytes, int elts) {
		this(hash, revision, version, uri, typeId, txId, 0, new Date(), owner, def_encoding, bytes, elts);
	}

	public XDMFragmentedDocument(long docKey, String uri, int typeId, long txStart, long txFinish, Date createdAt, String createdBy, String encoding, 
			int bytes, int elts) {
		this(toHash(docKey), toRevision(docKey), toVersion(docKey), uri, typeId, txStart, txFinish, createdAt, createdBy, encoding, bytes, elts);
	}
	
	public XDMFragmentedDocument(int hash, int revision, int version, String uri, int typeId, long txStart, long txFinish, Date createdAt, 
			String createdBy, String encoding, int bytes, int elts) {
		super(hash, revision, version, uri, typeId, txStart, txFinish, createdAt, createdBy, encoding, bytes, elts);
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
