package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class XDMDocumentType { 
	
	private int typeId;
	private String rootPath;
	private boolean normalized = false;
	private Set<XDMNamespace> schemas = new HashSet<XDMNamespace>();
	
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
	public Collection<XDMNamespace> getSchemas() {
		return Collections.unmodifiableCollection(schemas);
	}
	
	/**
	 * adds new schema
	 * @return boolean
	 */
	public boolean addSchema(XDMNamespace schema) {
		return schemas.add(schema);
	}

	/**
	 * removes schema
	 * @return boolean
	 */
	public boolean removeSchema(XDMNamespace schema) {
		return schemas.remove(schema);
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
