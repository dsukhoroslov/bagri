package com.bagri.core.server.api.df.json;

import java.util.Map;

import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ContentConverter;
import com.bagri.core.server.api.ContentHandler;
import com.bagri.core.server.api.ContentModeler;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentHandlerBase;

public class JsonpHandler extends ContentHandlerBase implements ContentHandler {
	
	private ContentBuilder<String> cb;
	private ContentModeler cm;
	private ContentParser<String> cp;
	private ContentConverter<String, Object> bc = null;
	private ContentConverter<String, Map<String, Object>> mc = null;
	
	public JsonpHandler(ModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}

	@Override
	public String getDataFormat() {
		return "JSON";
	}
	
	@Override
	public boolean isStringFormat() {
		return true;
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
	public ContentConverter<?, ?> getConverter(Class<?> source) {
		if (source.isAssignableFrom(Map.class)) {
			if (mc == null) {
				mc = new MapJsonConverter();
			}
			return mc;
		} else {
			if (bc == null) {
				bc = new BeanJsonConverter();
			}
			return bc;
		}
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

	private static class BeanJsonConverter implements ContentConverter<String, Object> {

		@Override
		public String convertFrom(Object source) {
			return null; //beanToXML(source);
		}

		@Override
		public Object convertTo(String content) {
			return null; //beanFromXML(content);
		}
		
	}
	
	private static class MapJsonConverter implements ContentConverter<String, Map<String, Object>> {

		@Override
		public String convertFrom(Map<String, Object> source) {
			return null; //mapToXML(source);
		}

		@Override
		public Map<String, Object> convertTo(String content) {
			return null; //mapFromXML(content);
		}
		
	}
}
