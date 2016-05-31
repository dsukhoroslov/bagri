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

import com.bagri.xdm.system.XDMCollection;
import com.bagri.xdm.system.XDMDataFormat;
import com.bagri.xdm.system.XDMDataStore;
import com.bagri.xdm.system.XDMFragment;
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
    XDMCollection.class,
    XDMFragment.class,
    XDMIndex.class,
    XDMJavaTrigger.class,
    XDMXQueryTrigger.class,
    //XDMIdentity.class,
    XDMRole.class,
    XDMUser.class,
    XDMDataFormat.class,
    XDMDataStore.class
})
public abstract class XDMEntity implements Convertable<Map<String, Object>>, Versionable {
	
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
	public Map<String, Object> convert() {
		Map<String, Object> result = new HashMap<>();
		result.put("version", version);
		result.put("created at", createdAt.toString());
		result.put("created by", createdBy);
		return result;
	}
	
	@Override
	public void updateVersion(String by) {
		version++;
		createdAt = new Date();
		createdBy = by;
	}

}
