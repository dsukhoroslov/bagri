package com.bagri.xdm.cache.hazelcast.store.xml;

class DocumentDataHolder {

	String uri;
	int docType = 0;
		
	DocumentDataHolder(String uri) {
		this.uri = uri;
	}

	@Override
	public String toString() {
		return "DocumentDataHolder [uri=" + uri + ", docType=" + docType + "]";
	}
	
}
