package com.bagri.core.server.api.df.map;

import java.util.Map;

import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ContentHandler;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentHandlerBase;

public class MapHandler extends ContentHandlerBase implements ContentHandler {
	
	private ContentBuilder<Map<String, Object>> cb;
	private ContentParser<Map<String, Object>> cp;
	
	public MapHandler(ModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}

	@Override
	public String getDataFormat() {
		return "MAP";
	}

	@Override
	public ContentBuilder<Map<String, Object>> getBuilder() {
		if (cb == null) {
			cb = new MapBuilder(modelMgr);
			cb.init(props);
		}
		return cb;
	}

	@Override
	public ContentParser<Map<String, Object>> getParser() {
		if (cp == null) {
			cp = new MapParser(modelMgr);
			cp.init(props);
		}
		return cp;
	}

}
