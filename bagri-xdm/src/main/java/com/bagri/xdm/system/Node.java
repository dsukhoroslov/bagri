package com.bagri.xdm.system;

import static com.bagri.xdm.common.XDMConstants.xdm_cluster_node_schemas;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.bagri.xdm.common.XDMEntity;

/**
 * Represents a collection of options which can be applied on XDM Schema node. 
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"name", 
		"options"
})
public class Node extends XDMEntity {
	
	/**
	 * possible roles implemented by node 
	 */
	@XmlType(name = "NodeRole", namespace = "http://www.bagridb.com/xdm/system")
	@XmlEnum
	public enum NodeRole {
		
		/**
		 * administrative node
		 */
	    @XmlEnumValue("admin")
		admin,

		/**
		 * cache server node
		 */
	    @XmlEnumValue("server")
		server;
		
	    /**
	     * 
	     * @param role the role to check
	     * @return true if the role nam e corresponds to administration role, false otherwise
	     */
		public static boolean isAdminRole(String role) {
			return admin.name().equals(role);
		}
	}
	
	@XmlAttribute(required = true)
	private String name;
	
	@XmlElement(name = "options")
	@XmlJavaTypeAdapter(EntriesAdapter.class)
	private Properties options = new Properties();

	/**
	 * default constructor
	 */
	public Node() {
		// we need it for JAXB
		super();
	}

	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param name the node name
	 * @param options the collection of node template options
	 */
	public Node(int version, Date createdAt, String createdBy, String name, Properties options) {
		super(version, createdAt, createdBy);
		this.name = name;
		setOptions(options);
	}

	/**
	 * 
	 * @return the node template name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return the schema names specified in {@code xdm.cluster.node.schemas} option
	 */
	public String[] getSchemas() {
		String schemas = getOption(xdm_cluster_node_schemas);
		if (schemas != null) {
			return schemas.split(" ");
		}
		return null;
	}
	
	/**
	 * 
	 * @return node template options
	 */
	public Properties getOptions() {
		return options;
	}
	
	/**
	 * 
	 * @param key the option name
	 * @return option value if found, null otherwise
	 */
	public String getOption(String key) {
		return options.getProperty(key);
	}
	
	/**
	 * 
	 * @param key the option name
	 * @param value the new option value
	 */
	public void setOption(String key, String value) {
		options.setProperty(key, value);
	}
	
	/**
	 * 
	 * @param options node template options
	 */
	public void setOptions(Properties options) {
		this.options.clear();
		if (options != null) {
			this.options.putAll(options);
		}
		// update version !?
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
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
		Node other = (Node) obj;
		if (!name.equals(other.name)) {
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
		result.put("name", name);
		//result.put("enabled", enabled);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "XDMNode [name=" + name + "; version=" + getVersion() + 
				"; created at=" + getCreatedAt() + "; by=" + getCreatedBy() + 
				"; options=" + options + "]";
	}
	
}
