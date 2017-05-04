package com.bagri.core.server.api.df.json;

import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ContentHandler;
import com.bagri.core.server.api.ContentModeler;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentHandlerBase;

public class JsonpHandler extends ContentHandlerBase implements ContentHandler {
	
	private ContentBuilder<String> cb;
	private ContentModeler cm;
	private ContentParser<String> cp;
	
	public JsonpHandler(ModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}

	@Override
	public String getDataFormat() {
		return "JSON";
	}

	@Override
	public ContentBuilder<String> getBuilder() {
		if (cb == null) {
			cb = new JsonpBuilder(modelMgr);
			cb.init(props);
		}
		return cb;
	}

	@Override
	public ContentModeler getModeler() {
		if (cm == null) {
			cm = new JsonpModeler(modelMgr);
			cm.init(props);
		}
		return cm;
	}

	@Override
	public ContentParser<String> getParser() {
		if (cp == null) {
			cp = new JsonpParser(modelMgr);
			cp.init(props);
		}
		return cp;
	}

}
