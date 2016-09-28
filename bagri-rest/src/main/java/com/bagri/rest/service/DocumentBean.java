package com.bagri.rest.service;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class DocumentBean {

	public String uri;
	public long createdAt;
	public String createdBy;
	public String encoding;
	public int size;

    public DocumentBean() {
    	//
    }
    
    public DocumentBean(String uri, long createdAt, String createdBy, String encoding, int size) {
		super();
		this.uri = uri;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.encoding = encoding;
		this.size = size;
	}

}
