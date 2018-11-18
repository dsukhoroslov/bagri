package com.bagri.core.server.api.df.map;

import static com.bagri.support.util.XMLUtils.mapFromXML;
import static com.bagri.support.util.XMLUtils.mapToXML;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.bagri.core.api.BagriException;
import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ContentConverter;
import com.bagri.core.server.api.ContentHandler;
import com.bagri.core.server.api.ContentMerger;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentHandlerBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class MapHandler extends ContentHandlerBase implements ContentHandler {
	
	private ContentBuilder<Map<String, Object>> cb;
	private ContentMerger<Map<String, Object>> cm;
	private ContentParser<Map<String, Object>> cp;
	private ContentConverter<Map<String, Object>, String> jc = null;
	private ContentConverter<Map<String, Object>, String> xc = null;
	
	public MapHandler(ModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}

	@Override
	public boolean isStringFormat() {
		return false;
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
	public ContentConverter<?, ?> getConverter(Class<?> source) {
		if (source.equals(ContentConverter.XmlConverter.class)) {
			if (xc == null) {
				xc = new XmlMapConverter();
			}
			return xc;
		} else if (source.equals(ContentConverter.JsonConverter.class)) {
			if (jc == null) {
				jc = new JacksonConverter();
			}
			return jc;
		}
		return null;
	}

	public ContentMerger<Map<String, Object>> getMerger() {
		if (cm == null) {
			cm = new MapMerger();
			cm.init(props);
		}
		return cm;
	}

	@Override
	public ContentParser<Map<String, Object>> getParser() {
		if (cp == null) {
			cp = new MapParser(modelMgr);
			cp.init(props);
		}
		return cp;
	}
	
	private static class MapMerger implements ContentMerger<Map<String, Object>> {

		@Override
		public void init(Properties properties) {
			// nothing to init
		}

		@Override
		public Map<String, Object> mergeContent(Map<String, Object> oldContent, Map<String, Object> newContent) throws BagriException {
			Map<String, Object> result = new HashMap<>(oldContent);
			result.putAll(newContent);
			return result;
		}
		
	}

	private static class JacksonConverter implements ContentConverter<Map<String, Object>, String> {
		
		private final ObjectReader r;
		private final ObjectWriter w;

		JacksonConverter() {
			ObjectMapper mapper = new ObjectMapper();
			r = mapper.readerFor(String.class);
			w = mapper.writerFor(Map.class).withDefaultPrettyPrinter();
		}

		@Override
		public Map<String, Object> convertFrom(String source) {
			try {
				return r.readValue(source);
			} catch (IOException ex) {
				// TODO log ex
				ex.printStackTrace();
				return null;
			}
		}

		@Override
		public String convertTo(Map<String, Object> content) {
			try {
				return w.writeValueAsString(content);
			} catch (IOException ex) {
				// TODO log ex
				ex.printStackTrace();
				return null;
			} 
		}
		
	}
	
	private static class XmlMapConverter implements ContentConverter<Map<String, Object>, String> {

		@Override
		public Map<String, Object> convertFrom(String source) {
			return mapFromXML(source);
		}

		@Override
		public String convertTo(Map<String, Object> content) {
			return mapToXML(content);
		}
		
	}
}
