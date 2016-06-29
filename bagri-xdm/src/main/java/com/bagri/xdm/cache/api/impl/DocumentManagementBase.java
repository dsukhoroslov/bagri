package com.bagri.xdm.cache.api.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.ModelManagement;
import com.bagri.xdm.domain.Data;
import com.bagri.xdm.domain.NodeKind;

/**
 * Base server-side document management component implementation. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class DocumentManagementBase extends com.bagri.xdm.api.impl.DocumentManagementBase  {

	protected ModelManagement model;
	
	public ModelManagement getModelManager() {
		return this.model;
	}
	
	public void setModelManager(ModelManagement model) {
		this.model = model;
	}

    public abstract Collection<String> buildDocument(Set<Long> docIds, String template, Map<String, String> params) throws XDMException;

	public Data getDataRoot(List<Data> elements) {
		for (Data data: elements) {
			if (data.getNodeKind() == NodeKind.element) {
				return data;
			}
		}
		return null;
	}
	
}
