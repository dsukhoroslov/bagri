package com.bagri.core.server.api.impl;

import static com.bagri.core.Constants.pn_client_contentSerializer;

import java.util.Properties;

import com.bagri.core.api.ContentSerializer;
import com.bagri.core.server.api.ContentConverter;
import com.bagri.core.server.api.ContentModeler;
import com.bagri.core.server.api.ModelManagement;

public abstract class ContentHandlerBase {

	protected ModelManagement modelMgr;
	protected ContentSerializer<?> cs; 
	protected Properties props = new Properties();

	public ContentConverter<?, ?> getConverter(Class<?> source) {
		return null;
	}

	public ContentModeler getModeler() {
		return null;
	}

	public ContentSerializer<?> getSerializer() {
		return cs;
	}
	
	public void init(Properties props) {
		if (props != null) {
			this.props.putAll(props);
			String csn = props.getProperty(pn_client_contentSerializer);
			if (csn != null) {
				try {
					Class<?> csc = Class.forName(csn);
					cs = (ContentSerializer<?>) csc.newInstance();
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
					//logger.warn("initializeSerializers. error instaintiating serializer '{}' for format '{}'", csc, csn, ex);
				}
			}
		}
	}
	
}
