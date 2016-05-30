package com.bagri.xdm.cache.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.cache.api.XDMDocumentManagement;
import com.bagri.xdm.client.common.impl.DocumentManagementBase;
import com.bagri.xdm.client.xml.XmlBuilder;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMNodeKind;

public abstract class XDMDocumentManagementServer extends DocumentManagementBase implements XDMDocumentManagement {

	protected XDMModelManagement model;
	
	public XDMModelManagement getModelManager() {
		return this.model;
	}
	
	public void setModelManager(XDMModelManagement model) {
		this.model = model;
	}

    public abstract Collection<String> buildDocument(Set<Long> docIds, String template, Map<String, String> params) throws XDMException;
	//public abstract XDMDocument createDocument(Entry<Long, XDMDocument> entry, String uri, String xml);
	//public abstract void deleteDocument(Entry<Long, XDMDocument> entry);
	//public abstract XDMDocument updateDocument(Entry<Long, XDMDocument> entry, boolean newVersion, String xml); // + audit info?
	
	//public abstract Source getDocumentSource(long docId);
	//public abstract void putDocumentSource(long docId, Source source);

	public XDMData getDataRoot(List<XDMData> elements) {
		for (XDMData data: elements) {
			if (data.getNodeKind() == XDMNodeKind.element) {
				return data;
			}
		}
		return null;
	}
	
}
