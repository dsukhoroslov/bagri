package com.bagri.xdm.client.common.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bagri.common.util.FileUtils.def_encoding;

import com.bagri.common.util.XMLUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.XDMDocument;

public abstract class XDMDocumentManagementBase {
	
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	public abstract String getDocumentAsString(long docId) throws XDMException;
	public abstract XDMDocument storeDocumentFromString(long docId, String uri, String xml) throws XDMException;
	
	public Source getDocumentAsSource(long docId) throws XDMException {
		String xml = getDocumentAsString(docId);
		if (xml != null) {
			return new StreamSource(new StringReader(xml));
		}
		return null;
	}
	
	public InputStream getDocumentAsSream(long docId) throws XDMException {
		String xml = getDocumentAsString(docId);
		if (xml != null) {
			try {
				return new ByteArrayInputStream(xml.getBytes(def_encoding));
			} catch (UnsupportedEncodingException ex) {
				throw new XDMException(ex, XDMException.ecInOut);
			}
		}
		return null;
	}
	
	public XDMDocument storeDocumentFromSource(long docId, String uri, Source source) throws XDMException {
		try {
			String xml = XMLUtils.sourceToString(source);
			return storeDocumentFromString(docId, uri, xml);
		} catch (IOException ex) {
			logger.error("storeDocumentFromSource.error; " + ex.getMessage(), ex);
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}
	
	public XDMDocument storeDocumentFromStream(long docId, String uri, InputStream stream) throws XDMException {
		try {
			String xml = XMLUtils.textToString(stream);
			return storeDocumentFromString(docId, uri, xml);
		} catch (IOException ex) {
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}

}
