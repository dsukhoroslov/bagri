package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents XDM document meta-data.
 *  
 * @author Denis Sukhoroslov
 * @since 05.2013 
 * @version 0.2
 */
public class XDMDocumentType { 
	
	private int typeId;
	private String rootPath;
	private boolean normalized = false;
	private int fragmentationLimit = 0;
	private Set<Long> fragments = new HashSet<>();
	private Set<String> schemas = new HashSet<>();
	
	public XDMDocumentType() {
		//
	}
	
	public XDMDocumentType(int typeId, String rootPath) {
		this.typeId = typeId;
		this.rootPath = rootPath;
	}

	/**
	 * @return the typeId
	 */
	public int getTypeId() {
		return typeId;
	}

	/**
	 * @return the typeName
	 */
	public String getRootPath() {
		return rootPath;
	}
	
	/**
	 * @return the normalized
	 */
	public boolean isNormalized() {
		return normalized;
	}

	/**
	 * @param normalized the normalized to set
	 */
	public void setNormalized(boolean normalized) {
		this.normalized = normalized;
	}
	
	/**
	 * @return the schemas
	 */
	public Collection<String> getSchemas() {
		return Collections.unmodifiableCollection(schemas);
	}
	
	/**
	 * adds new schema
	 * @return boolean
	 */
	public boolean addSchema(String schemaUri) {
		return schemas.add(schemaUri);
	}

	/**
	 * removes schema
	 * @return boolean
	 */
	public boolean removeSchema(String schemaUri) {
		return schemas.remove(schemaUri);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMDocumentType [typeId=" + typeId + ", rootPath=" + rootPath
				+ ", schemas=" + schemas + "]";
	}

}
