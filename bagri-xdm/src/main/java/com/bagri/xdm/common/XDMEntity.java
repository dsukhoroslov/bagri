package com.bagri.xdm.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.bagri.xdm.system.XDMIdentity;
import com.bagri.xdm.system.XDMIndex;
import com.bagri.xdm.system.XDMJavaTrigger;
import com.bagri.xdm.system.XDMLibrary;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMNode;
import com.bagri.xdm.system.XDMRole;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xdm.system.XDMUser;
import com.bagri.xdm.system.XDMXQueryTrigger;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(namespace = "http://www.bagri.com/xdm/system",	propOrder = {
@XmlType(propOrder = {
		"version", 
		"createdAt", 
		"createdBy"
})
@XmlSeeAlso({
    XDMNode.class,
    XDMSchema.class,
    XDMModule.class,
    XDMLibrary.class,
    XDMIndex.class,
    XDMJavaTrigger.class,
    XDMXQueryTrigger.class,
    //XDMIdentity.class,
    XDMRole.class,
    XDMUser.class
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

	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap<>();
		result.put("version", getVersion());
		result.put("created at", getCreatedAt().toString());
		result.put("created by", getCreatedBy());
		return result;
	}
	
	@Override
	public void updateVersion(String by) {
		version++;
		createdAt = new Date();
		createdBy = by;
	}

}
