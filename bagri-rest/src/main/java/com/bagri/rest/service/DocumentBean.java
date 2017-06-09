package com.bagri.rest.service;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class DocumentBean {

	public String uri;
	public long createdAt;
	public String createdBy;
	public String format;
	public String encoding;
	public int size;

    public DocumentBean() {
    	//
    }
    
    public DocumentBean(String uri, long createdAt, String createdBy, String format, String encoding, int size) {
		super();
		this.uri = uri;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.format = format;
		this.encoding = encoding;
		this.size = size;
	}

}
