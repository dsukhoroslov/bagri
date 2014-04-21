package com.bagri.xdm.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.common.XDMHelper;

/**
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 * @version 0.1
 */
public class XDMIndex<V> { //implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7780583844744846834L;
	
	private String path;
	private V value;
	private Set<XDMDataKey> references  = new HashSet<XDMDataKey>();
	private XDMFactory factory = XDMHelper.getXDMFactory();

	public XDMIndex() {
		//factory = XDMHelper.getXDMFactory();
	}
	
	public XDMIndex(String path, V value, Set<? extends XDMDataKey> references) {
		this();
		this.path = path;
		this.value = value;
		if (references != null) {
			for (XDMDataKey ref: references) {
				addIndex(ref.getDocumentId(), ref.getDataId());
			}
		}
	}

	public XDMIndex(String path, V value, long documentId, long dataId) {
		super();
		this.path = path;
		this.value = value;
		addIndex(documentId, dataId);
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the value
	 */
	public V getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(V value) {
		this.value = value;
	}

	/**
	 * @return the docIds
	 */
	public Set<Long> getDataIds() {
		Set<Long> dataIds = new HashSet<Long>();
		for (XDMDataKey ref: references) {
			dataIds.add(ref.getDataId());
		}
		return dataIds;
	}

	/**
	 * @return the docIds
	 */
	public Set<Long> getDocumentIds() {
		Set<Long> docIds = new HashSet<Long>();
		for (XDMDataKey ref: references) {
			docIds.add(ref.getDocumentId());
		}
		return docIds;
	}

	/**
	 * @return the elementIds
	 */
	public Set<? extends XDMDataKey> getReferences() {
		return Collections.unmodifiableSet(references);
	}

	/**
	 * @param elementIds the elementIds to set
	 */
	public void setReferences(Set<? extends XDMDataKey> references) {
		this.references.clear();
		if (references != null && references.size() > 0) {
			for (XDMDataKey ref: references) {
				addIndex(ref.getDocumentId(), ref.getDataId());
			}
		}
	}
	
	public void addIndex(long documentId, long dataId) {
		if (factory == null) {
			// log this...
			factory = XDMHelper.getXDMFactory();
		}
		this.references.add(factory.newXDMDataKey(dataId, documentId));
	}
	
}
