package com.bagri.xdm.cache.api.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMNodeKind;

// not sure, why do we need this class at all..
public abstract class DocumentManagementBase extends com.bagri.xdm.api.impl.DocumentManagementBase  {

	protected XDMModelManagement model;
	
	public XDMModelManagement getModelManager() {
		return this.model;
	}
	
	public void setModelManager(XDMModelManagement model) {
		this.model = model;
	}

    public abstract Collection<String> buildDocument(Set<Long> docIds, String template, Map<String, String> params) throws XDMException;

	public XDMData getDataRoot(List<XDMData> elements) {
		for (XDMData data: elements) {
			if (data.getNodeKind() == XDMNodeKind.element) {
				return data;
			}
		}
		return null;
	}
	
}
