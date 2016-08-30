package com.bagri.xdm.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for Document versions
 * 
 * @author Denis Sukhoroslov
 *
 */
public class DocumentChain {
	
	private String uri;
	private List<Document> docChain = new ArrayList<>();
	
	/**
	 * default constructor
	 */
	public DocumentChain() {
		//
	}
	
	/**
	 * 
	 * @param uri the common documents uri 
	 */
	public DocumentChain(String uri) {
		this.uri = uri;
	}
	
	/**
	 * 
	 * @return the common documents uri
	 */
	public String getUri() {
		return uri;
	}
	
	/**
	 * 
	 * @return the latest active document in chain
	 */
	public Document getActiveDocument() {
		return docChain.get(docChain.size() - 1);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "DocumentChain [uri=" + uri + ", chain=" + docChain.size() + "]";
	}

}
