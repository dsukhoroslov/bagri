package com.bagri.xdm.client.common.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bagri.common.util.FileUtils.def_encoding;

import com.bagri.common.util.XMLUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.XDMDocument;

public abstract class DocumentManagementBase {
	
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	public abstract String getDocumentAsString(String uri) throws XDMException;
	public abstract XDMDocument storeDocumentFromString(String uri, String xml, Properties props) throws XDMException;
	
	public Source getDocumentAsSource(String uri) throws XDMException {
		String content = getDocumentAsString(uri);
		if (content != null) {
			return new StreamSource(new StringReader(content));
		}
		return null;
	}
	
	public InputStream getDocumentAsSream(String uri) throws XDMException {
		String content = getDocumentAsString(uri);
		if (content != null) {
			try {
				return new ByteArrayInputStream(content.getBytes(def_encoding));
			} catch (UnsupportedEncodingException ex) {
				throw new XDMException(ex, XDMException.ecInOut);
			}
		}
		return null;
	}
	
	public XDMDocument storeDocumentFromSource(String uri, Source source, Properties props) throws XDMException {
		try {
			// TODO: get serialization props only..
			String xml = XMLUtils.sourceToString(source, null);
			return storeDocumentFromString(uri, xml, props);
		} catch (IOException ex) {
			logger.error("storeDocumentFromSource.error", ex);
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}
	
	public XDMDocument storeDocumentFromStream(String uri, InputStream stream, Properties props) throws XDMException {
		try {
			// TODO: get serialization props only..
			String xml = XMLUtils.textToString(stream);
			return storeDocumentFromString(uri, xml, props);
		} catch (IOException ex) {
			logger.error("storeDocumentFromStream.error", ex);
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}

}
