package com.bagri.xdm.api;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system",
	propOrder = {
		"version", 
		"createdAt", 
		"createdBy"
})
public abstract class XDMEntity implements Versionable {
	
	@XmlElement(required = true)
	private int version;

	@XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
	private Date createdAt;

	@XmlElement(required = true)
	private String createdBy;
	
	public XDMEntity() {
		// ...
	}
	
	public XDMEntity(int version, Date createdAt, String createdBy) {
		this.version = version;
		// todo: think about other Date implementation, joda date, for instance..
		this.createdAt = new Date(createdAt.getTime());
		this.createdBy = createdBy;
	}

	@Override
	public int getVersion() {
		return version;
	}

	@Override
	public Date getCreatedAt() {
		return new Date(createdAt.getTime());
	}

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	@Override
	public void updateVersion(String by) {
		version++;
		createdAt = new Date();
		createdBy = by;
	}

}
