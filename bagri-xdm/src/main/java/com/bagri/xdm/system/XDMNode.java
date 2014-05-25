package com.bagri.xdm.system;

import java.util.Date;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.bagri.xdm.api.XDMEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"name", 
		"address", 
		"options"
})
public class XDMNode extends XDMEntity {
	
	@XmlType(name = "NodeRole", namespace = "http://www.bagri.com/xdm/system")
	@XmlEnum
	public enum NodeRole {
		
	    @XmlEnumValue("admin")
		admin,

	    @XmlEnumValue("server")
		server;
		
		public static boolean isAdminRole(String role) {
			return admin.name().equals(role);
		}
	}
	
    public static final String op_node_name = "xdm.cluster.node.name";
    public static final String op_node_role = "xdm.cluster.node.role";
    public static final String op_node_size = "xdm.cluster.node.size";
    public static final String op_node_schemas = "xdm.cluster.node.schemas";

	@XmlAttribute(required = true)
	private String name;
	
	@XmlElement(required = true)
	private String address;
	
	@XmlElement(name = "options")
	@XmlJavaTypeAdapter(XDMEntriesAdapter.class)
	private Properties options = new Properties();

	public XDMNode() {
		// we need it for JAXB
		super();
	}

	public XDMNode(int version, Date createdAt, String createdBy, String name, String address, Properties options) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.address = address;
		setOptions(options);
	}

	public String getName() {
		return name;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getNode() {
		return name + "[" + address + "]";
	}
	
	public String[] getSchemas() {
		String schemas = getOption(op_node_schemas);
		return schemas.split(" ");
	}
	
	public Properties getOptions() {
		return options;
	}
	
	public String getOption(String key) {
		return options.getProperty(key);
	}
	
	public void setOption(String key, String value) {
		options.setProperty(key, value);
	}
	
	public void setOptions(Properties options) {
		this.options.clear();
		if (options != null) {
			this.options.putAll(options);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		result = prime * result + address.hashCode();
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
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
		XDMNode other = (XDMNode) obj;
		if (!name.equals(other.name)) {
			return false;
		}
		if (!address.equals(other.address)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMNode [name=" + name + "; addess=" + address + 
				"; version=" + getVersion() + "; created at=" + getCreatedAt() + 
				"; by=" + getCreatedBy() + "; options=" + options + "]";
	}
	
}
