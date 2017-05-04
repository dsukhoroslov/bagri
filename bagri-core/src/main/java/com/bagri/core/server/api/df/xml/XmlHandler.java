package com.bagri.core.server.api.df.xml;

import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ContentHandler;
import com.bagri.core.server.api.ContentModeler;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentHandlerBase;

public class XmlHandler extends ContentHandlerBase implements ContentHandler {
	
	private ContentBuilder<String> cb = null;
	private ContentModeler cm = null;
	private ContentParser<String> cp = null;
	
	public XmlHandler(ModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}

	@Override
	public String getDataFormat() {
		return "XML";
	}

	@Override
	public ContentBuilder<String> getBuilder() {
		if (cb == null) {
			cb = new XmlBuilder(modelMgr);
			cb.init(props);
		}
		return cb;
	}

	@Override
	public ContentModeler getModeler() {
		if (cm == null) {
			cm = new XmlModeler(modelMgr);
			cm.init(props);
		}
		return cm;
	}

	@Override
	public ContentParser<String> getParser() {
		if (cp == null) {
			cp = new XmlStaxParser(modelMgr);
			cp.init(props);
		}
		return cp;
	}
	
}
