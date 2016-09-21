package com.bagri.rest.docs;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentResource {

    @XmlElement(name = "uri")
	private String uri;

    @XmlElement(name = "createdAt")
	private long createdAt;
    
    @XmlElement(name = "createdBy")
	private String createdBy;
    
    @XmlElement(name = "encoding")
    private String encoding;
    
    @XmlElement(name = "size")
	private int size;

    public DocumentResource() {
    	//
    }
    
    public DocumentResource(String uri, long createdAt, String createdBy, String encoding, int size) {
		super();
		this.uri = uri;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.encoding = encoding;
		this.size = size;
	}

    
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return the createdAt
	 */
	public long getCreatedAt() {
		return createdAt;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}


}
