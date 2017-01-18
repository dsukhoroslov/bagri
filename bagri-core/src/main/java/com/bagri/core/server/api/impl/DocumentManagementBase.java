package com.bagri.core.server.api.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;
import com.bagri.core.model.NodeKind;
import com.bagri.core.server.api.ModelManagement;

/**
 * Base server-side document management component implementation. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class DocumentManagementBase extends com.bagri.core.api.impl.DocumentManagementBase  {

	protected ModelManagement model;
	
	public ModelManagement getModelManager() {
		return this.model;
	}
	
	public void setModelManager(ModelManagement model) {
		this.model = model;
	}

    public abstract Collection<String> buildDocument(Set<Long> docIds, String template, Map<String, Object> params) throws BagriException;

	public Data getDataRoot(List<Data> elements) {
		for (Data data: elements) {
			if (data.getNodeKind() == NodeKind.element) {
				return data;
			}
		}
		return null;
	}
	
}
