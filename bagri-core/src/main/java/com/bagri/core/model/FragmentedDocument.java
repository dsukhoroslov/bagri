package com.bagri.core.model;

import static com.bagri.support.util.FileUtils.def_encoding;

import java.util.Date;

/**
 * Extends XDM document, contains fragments
 *  
 * @author Denis Sukhoroslov
 * @since 05.2014 
 */
public class FragmentedDocument extends Document {

	private long[] fragments;
	
	/**
	 * {@inheritDoc}
	 */
	public FragmentedDocument() {
		super();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public FragmentedDocument(long docKey, String uri, String root, String owner, long txId, int bytes, int elts) {
		this(docKey, uri, root, txId, 0, new Date(), owner, cte_xml_utf8, bytes, elts);
	}

	/**
	 * {@inheritDoc} 
	 */
	public FragmentedDocument(long docKey, String uri, String root, long txStart, long txFinish, Date createdAt, 
			String createdBy, String format, int bytes, int elts) {
		super(docKey, uri, root, txStart, txFinish, createdAt, createdBy, format, bytes, elts);
	}
	
	public long[] getFragments() {
		return fragments;
	}
	
	/**
	 * set document fragments
	 * 
	 * @param fragments the document fragment ids
	 */
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
