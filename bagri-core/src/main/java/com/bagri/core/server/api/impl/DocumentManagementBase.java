package com.bagri.core.server.api.impl;

import java.util.List;

import com.bagri.core.model.Data;
import com.bagri.core.model.NodeKind;
import com.bagri.core.server.api.ModelManagement;

/**
 * Base server-side document management component implementation. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class DocumentManagementBase {

	protected ModelManagement model;
	
	public ModelManagement getModelManager() {
		return this.model;
	}
	
	public void setModelManager(ModelManagement model) {
		this.model = model;
	}

	public Data getDataRoot(List<Data> elements) {
		for (Data data: elements) {
			if (data.getNodeKind() == NodeKind.document) { 
				return data;
			}
		}
		return null;
	}
	
}
