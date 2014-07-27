package com.bagri.xdm.cache.hazelcast.store.xml;

class DocumentDataHolder {

	long docId;
	int docType = 0;
		
	DocumentDataHolder(long docId) {
		this.docId = docId;
	}

}
