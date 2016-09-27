package com.bagri.rest.service;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CollectionParams {

	public String[] collections;
	public boolean add;

	public CollectionParams() {
		// de-ser
	}
	
	public CollectionParams(String[] collections, boolean add) {
		this.collections = collections;
		this.add = add;
	}
	
	@Override
	public String toString() {
		return "CollectionParams [collections=" + collections + "; add=" + add + "]"; 
	}
	
}
