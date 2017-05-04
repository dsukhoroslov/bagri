package com.bagri.core.server.api.impl;

import java.util.Properties;

import com.bagri.core.server.api.ContentModeler;
import com.bagri.core.server.api.ModelManagement;

public abstract class ContentHandlerBase {

	protected ModelManagement modelMgr;
	protected Properties props = new Properties();

	public ContentModeler getModeler() {
		return null;
	}

	public void init(Properties props) {
		if (props != null) {
			this.props.putAll(props);
		}
	}
	
}
