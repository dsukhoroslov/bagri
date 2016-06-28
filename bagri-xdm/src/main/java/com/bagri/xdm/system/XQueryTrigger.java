package com.bagri.xdm.system;

import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents trigger implemented in XQuery.
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="xqtrigger", namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"module", 
		"function"
})
public class XQueryTrigger extends TriggerDefinition {

	@XmlElement(required = true)
	private String module;
		
	@XmlElement(required = true)
	private String function;

	/**
	 * default constructor
	 */
	public XQueryTrigger() {
		// for JAXB de-serialization
		super();
	}

	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param module the XQuery module containing function which implements trigger
	 * @param function the XQuery function implementing trigger
	 * @param docType the document type for which trigger is registered
	 * @param synchronous is trigger invoked synchronously or not
	 * @param enabled the trigger enabled flag
	 * @param index the order at which trigger will be invoked 
	 */
	public XQueryTrigger(int version, Date createdAt, String createdBy, String module, 
			String function, String docType, boolean synchronous, boolean enabled, int index) {
		super(version, createdAt, createdBy, docType, synchronous, enabled, index);
		this.module = module;
		this.function = function;
	}
	
	/**
	 * 
	 * @return the trigger name
	 */
	public String getName() {
		return module + ":" + function;
	}

	/**
	 * 
	 * @return the XQuery module containing function which implements trigger 
	 */
	public String getModule() {
		return module;
	}

	/**
	 * 
	 * @return the XQuery function implementing trigger
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + module.hashCode();
		result = prime * result	+ function.hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		XQueryTrigger other = (XQueryTrigger) obj;
		if (!module.equals(other.module)) {
			return false;
		}
		if (!function.equals(other.function)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("module", module);
		result.put("function", function);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "XQueryTrigger [module=" + module + ", function=" + function
				+ ", docType=" + getDocType() + ", enabled=" + isEnabled()
				+ ", actions=" + getActions().toString() + "]";
	}
	

}
