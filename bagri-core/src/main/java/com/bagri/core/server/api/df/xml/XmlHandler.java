package com.bagri.core.server.api.df.xml;

import static com.bagri.support.util.XMLUtils.*;

import java.util.Map;

import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ContentConverter;
import com.bagri.core.server.api.ContentHandler;
import com.bagri.core.server.api.ContentModeler;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentHandlerBase;

public class XmlHandler extends ContentHandlerBase implements ContentHandler {
	
	private ContentBuilder<String> cb = null;
	private ContentModeler cm = null;
	private ContentParser<String> cp = null;
	private ContentConverter<String, Object> bc = null;
	private ContentConverter<String, Map<String, Object>> mc = null;
	
	public XmlHandler(ModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}

	@Override
	public String getDataFormat() {
		return "XML";
	}

	@Override
	public boolean isStringFormat() {
		return true;
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
	public ContentConverter<?, ?> getConverter(Class<?> source) {
		if (source.isAssignableFrom(Map.class)) {
			if (mc == null) {
				mc = new MapXmlConverter();
			}
			return mc;
		} else {
			if (bc == null) {
				bc = new BeanXmlConverter();
			}
			return bc;
		}
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
	
	private static class BeanXmlConverter implements ContentConverter<String, Object> {

		@Override
		public String convertFrom(Object source) {
			return beanToXML(source);
		}

		@Override
		public Object convertTo(String content) {
			return beanFromXML(content);
		}
		
	}
	
	private static class MapXmlConverter implements ContentConverter<String, Map<String, Object>> {

		@Override
		public String convertFrom(Map<String, Object> source) {
			return mapToXML(source);
		}

		@Override
		public Map<String, Object> convertTo(String content) {
			return mapFromXML(content);
		}
		
	}

}
