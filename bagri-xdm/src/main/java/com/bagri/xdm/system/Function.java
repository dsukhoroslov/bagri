package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents external function registered in XDMLibrary and to be used from XQuery 
 *  
 * @author Denis Sukhoroslov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"className",
		"method",
		"result",
		"description",
		"prefix",
		"parameters"
})
public class Function {

	@XmlElement(required = true)
	private String className;

	@XmlElement(required = true)
	private String method;

	@XmlElement(required = true)
	private DataType result;
	
	@XmlElement(required = false)
	private String description;

	@XmlElement(required = true)
	private String prefix;
	
	@XmlElement(name="parameter")
	@XmlElementWrapper(name="parameters")
	private List<Parameter> parameters = new ArrayList<>();
	
	@XmlTransient
	private Map<String, List<String>> annotations = new HashMap<>();

	/**
	 * default constructor
	 */
	public Function() {
		// for JAXB
		super();
	}
	
	/**
	 * 
	 * @param className the class name implementing function 
	 * @param method the class method implementing function
	 * @param result the type of returning result
	 * @param description the function description
	 * @param prefix the namespace prefix to be used in XQuery function declaration
	 */
	public Function(String className, String method, DataType result, String description, String prefix) {
		this.className = className;
		this.method = method;
		this.result = result;
		this.description = description;
		this.prefix = prefix;
	}
	
	/**
	 * 
	 * @return the class name implementing function 
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * 
	 * @return the class method implementing function
	 */
	public String getMethod() {
		return method;
	}
	
	/**
	 * 
	 * @return the type of returning result
	 */
	public DataType getResult() {
		return result;
	}
	
	/**
	 * 
	 * @return the function description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * 
	 * @return the namespace prefix to be used in XQuery function declaration
	 */
	public String getPrefix() {
		return prefix;
	}
	
	/**
	 * 
	 * @return the list of function parameters
	 */
	public List<Parameter> getParameters() {
		return parameters;
	}
	
	/**
	 * 
	 * @return the list of function annotations
	 */
	public Map<String, List<String>> getAnnotations() {
		return annotations;
	}
	
	/**
	 * adds new annotation
	 * 
	 * @param name the annotation name
	 * @param value the annotation value
	 */
	public void addAnnotation(String name, String value) {
		List<String> values = annotations.get(name);
		if (values == null) {
			values = new ArrayList<>();
			annotations.put(name,  values);
		}
		if (value != null) {
			values.add(value);
		}
	}
	
	/**
	 * 
	 * @return the complete function signature
	 */
	public String getSignature() {

		StringBuffer buff = new StringBuffer();
		buff.append(className).append(".");
		buff.append(method).append("(");
		int idx = 0;
		for (Parameter xp: parameters) {
			if (idx > 0) {
				buff.append(", ");
			}
			buff.append(xp.getName()).append(" ").append(xp.getType());
			idx++;
		}
		buff.append("): ").append(result.getType()).append(";");
		return buff.toString();
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return getSignature().hashCode();
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
		Function other = (Function) obj;
		return getSignature().equals(other.getSignature());
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return prefix + ":" + getSignature();
	}
	
}
