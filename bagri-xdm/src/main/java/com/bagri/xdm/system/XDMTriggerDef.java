package com.bagri.xdm.system;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import com.bagri.xdm.common.XDMEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"docType",
		"synchronous",
		"enabled",
		"actions"
})
public abstract class XDMTriggerDef extends XDMEntity {
	
	@XmlElement(required = false)
	private String docType;

	@XmlElement(required = true)
	private boolean synchronous;
	
	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	@XmlElement(name="action")
	@XmlElementWrapper(name="actions")
	private Set<XDMTriggerAction> actions = new HashSet<XDMTriggerAction>();
	
	public XDMTriggerDef() {
		// for JAXB
		super();
	}
	
	public XDMTriggerDef(int version, Date createdAt, String createdBy, String docType, 
			boolean synchronous, boolean enabled) {
		super(version, createdAt, createdBy);
		this.docType = docType;
		this.synchronous = synchronous;
		this.enabled = enabled;
	}
	
	public abstract String getName();

	public String getDocType() {
		return docType;
	}

	public boolean isSynchronous() {
		return synchronous;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			//this.updateVersion("???");
			return true;
		}
		return false;
	}

	public Set<XDMTriggerAction> getActions() {
		return actions;
	}
	
	public void setActions(Collection<XDMTriggerAction> actions) {
		this.actions.clear();
		if (actions != null) {
			this.actions.addAll(actions);
		}
	}
	
	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap<>();
		result.put("version", getVersion());
		result.put("created at", getCreatedAt().toString());
		result.put("created by", getCreatedBy());
		//result.put("library", library);
		//result.put("className", className);
		result.put("docType", docType); // can be null!
		result.put("synchronous", synchronous); 
		result.put("enabled", enabled);
		result.put("actions", actions.toString());
		return result;
	}

}
