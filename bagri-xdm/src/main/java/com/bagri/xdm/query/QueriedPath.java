package com.bagri.xdm.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents resolved query path  
 * 
 * @author Denis Sukhoroslov
 *
 */
public class QueriedPath {
	
	private int dataType;
	private boolean indexed;
	private List<Integer> pathIds = new ArrayList<>(2);
	
	/**
	 * default constructor
	 */
	public QueriedPath() {
		//
	}
	
	/**
	 * 
	 * @param dataType the XQJ data type
	 * @param indexed is path indexed or not
	 * @param pathIds resolved model path identifiers
	 */
	public QueriedPath(int dataType, boolean indexed, Collection<Integer> pathIds) {
		this.dataType = dataType;
		this.indexed = indexed;
		this.pathIds.addAll(pathIds);
	}

	/**
	 * 
	 * @return the XQJ data type
	 */
	public int getDataType() {
		return dataType;
	}
	
	/**
	 * 
	 * @return is path indexed or not
	 */
	public boolean isIndexed() {
		return indexed;
	}
	
	/**
	 * 
	 * @return resolved model path identifiers
	 */
	public Collection<Integer> getPathIds() {
		return pathIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "QueriedPath [dataType=" + dataType + ", indexed=" + indexed	+ ", pathIds=" + pathIds + "]";
	}

	
}
