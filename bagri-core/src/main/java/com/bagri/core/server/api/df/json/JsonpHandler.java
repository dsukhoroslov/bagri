package com.bagri.core.server.api.df.json;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ContentConverter;
import com.bagri.core.server.api.ContentHandler;
import com.bagri.core.server.api.ContentModeler;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentHandlerBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonpHandler extends ContentHandlerBase implements ContentHandler {
	
	private ContentBuilder<String> cb;
	private ContentModeler cm;
	private ContentParser<String> cp;
	private ConcurrentHashMap<Class<?>, ContentConverter<String, Object>> ccs = new ConcurrentHashMap<>();
	
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
		ContentConverter<String, Object> cc = ccs.get(source);
		if (cc == null) {
			cc = new JacksonConverter(source);
			ccs.putIfAbsent(source, cc);
		}
		return cc;
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

	private static class JacksonConverter implements ContentConverter<String, Object> {
		
		private final ObjectReader r;
		private final ObjectWriter w;

		JacksonConverter(Class<?> source) {
			ObjectMapper mapper = new ObjectMapper();
			r = mapper.readerFor(source);
			w = mapper.writerFor(source);
		}

		@Override
		public String convertFrom(Object source) {
			try {
				return w.writeValueAsString(source);
			} catch (JsonProcessingException ex) {
				// TODO log ex
				ex.printStackTrace();
				return null;
			}
		}

		@Override
		public Object convertTo(String content) {
			try {
				return r.readValue(content);
			} catch (IOException ex) {
				// TODO log ex
				ex.printStackTrace();
				return null;
			} 
		}
		
	}
	
}
