package com.bagri.xdm.cache.hazelcast.store.xml;

public abstract class XmlCacheStore {

	private String dataPath;

	public String getDataPath() {
		return dataPath;
	}
	
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}
	
}
