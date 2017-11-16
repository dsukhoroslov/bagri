package com.bagri.core.system;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents basic trigger implementation
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/schema/system", propOrder = {
		"synchronous",
		"enabled",
		"collection",
		"actions"
})
@XmlSeeAlso({
    JavaTrigger.class,
    XQueryTrigger.class
})
public abstract class TriggerDefinition extends Entity {
	
	@XmlElement(required = true)
	private boolean synchronous;
	
	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	@XmlElement(required = false)
	private String collection;

	@XmlElement(name="action")
	@XmlElementWrapper(name="actions")
	private Set<TriggerAction> actions = new HashSet<TriggerAction>();
	
	/**
	 * default constructor
	 */
	public TriggerDefinition() {
		// for JAXB
		super();
	}
	
	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param synchronous is trigger invoked synchronously or not
	 * @param enabled the trigger enabled flag
	 * @param collection the documents collection for which trigger is registered
	 */
	public TriggerDefinition(int version, Date createdAt, String createdBy,	boolean synchronous, boolean enabled, String collection) {
		super(version, createdAt, createdBy);
		this.synchronous = synchronous;
		this.enabled = enabled;
		this.collection = collection;
	}

	/**
	 * 
	 * @return the trigger name
	 */
	public abstract String getName();

	public String getCollection() {
		return collection;
	}

	/**
	 * 
	 * @return is trigger invoked synchronously or not
	 */
	public boolean isSynchronous() {
		return synchronous;
	}

	/**
	 * 
	 * @return the trigger enabled flag
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * 
	 * @param enabled the new flag value
	 * @return true if flag has been changed, false otherwise
	 */
	public boolean setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			//this.updateVersion("???");
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param index action fire index
	 * @param order action fire order (before/after)
	 * @param scope action fire scope
	 */
	public void addAction(int index, TriggerAction.Order order, TriggerAction.Scope scope) {
		actions.add(new TriggerAction(index, order, scope));
	}
	
	/**
	 * 
	 * @return a collection of trigger actions  
	 */
	public Set<TriggerAction> getActions() {
		return actions;
	}
	
	/**
	 * 
	 * @param actions a collection of trigger actions
	 */
	public void setActions(Collection<TriggerAction> actions) {
		this.actions.clear();
		if (actions != null) {
			this.actions.addAll(actions);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("collection", collection); // can be null!
		result.put("synchronous", synchronous); 
		result.put("enabled", enabled);
		result.put("actions", actions.toString());
		return result;
	}

}
