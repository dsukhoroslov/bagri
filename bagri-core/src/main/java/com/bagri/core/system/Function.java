package com.bagri.core.system;

import java.util.ArrayList;
import java.util.Collections;
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

import com.bagri.support.util.PropUtils;

/**
 * Represents external function registered in XDMLibrary and to be used from XQuery 
 *  
 * @author Denis Sukhoroslov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/schema/system", propOrder = {
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
	private Map<String, List<List<String>>> annotations = new HashMap<>();

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
	public Map<String, List<List<String>>> getAnnotations() {
		return annotations;
	}
	
	/**
	 * 
	 * @param aName the annotation name
	 * @return the combined list of all annotation values
	 */
	public List<String> getFlatList(String aName) {
		List<List<String>> values = annotations.get(aName);
		if (values != null && values.size() > 0) {
			List<String> flat = new ArrayList<>(values.size());
			for (List<String> vals: values) {
				for (String value: vals) {
					flat.add(value);
				}
			}
			return flat;
		}
		return Collections.emptyList();
	}
	
	/**
	 * adds new annotation
	 * 
	 * @param name the annotation name
	 * @param values the annotation value
	 */
	public void addAnnotation(String name, List<String> values) {
		List<List<String>> vList = annotations.get(name);
		if (vList == null) {
			vList = new ArrayList<>();
			annotations.put(name, vList);
		}
		if (values != null) {
			vList.add(new ArrayList<>(values)); 
		}
	}
	
	/**
	 * 
	 * @return the complete function signature
	 */
	public String getSignature() {

		StringBuilder buff = new StringBuilder();
		if (className != null) {
			buff.append(className).append(".");
		} else if (prefix != null) {
			buff.append(prefix).append(":");
		}
		buff.append(method).append("(");
		int idx = 0;
		for (Parameter xp: parameters) {
			if (idx > 0) {
				buff.append(", ");
			}
			buff.append(xp.getName()).append(" ").append(xp.getType()).append(xp.getCardinality().shortPresentation());
			idx++;
		}
		buff.append("): ").append(result.getType()).append(result.getCardinality().shortPresentation()); //.append(";");
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
		return getSignature();
	}
	
}
