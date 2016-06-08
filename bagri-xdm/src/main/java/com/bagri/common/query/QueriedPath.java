package com.bagri.common.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueriedPath {
	
	private int dataType;
	private boolean indexed;
	private List<Integer> pathIds = new ArrayList<>(2);
	
	public QueriedPath() {
		//
	}
	
	public QueriedPath(int dataType, boolean indexed, Collection<Integer> pathIds) {
		this.dataType = dataType;
		this.indexed = indexed;
		this.pathIds.addAll(pathIds);
	}

	public int getDataType() {
		return dataType;
	}
	
	public boolean isIndexed() {
		return indexed;
	}
	
	public Collection<Integer> getPathIds() {
		return pathIds;
	}

	@Override
	public String toString() {
		return "QueriedPath [dataType=" + dataType + ", indexed=" + indexed
				+ ", pathIds=" + pathIds + "]";
	}

	
}
