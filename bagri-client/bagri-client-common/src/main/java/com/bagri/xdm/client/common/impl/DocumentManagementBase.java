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
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;

public abstract class DocumentManagementBase {
	
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	public abstract String getDocumentAsString(XDMDocumentId docId) throws XDMException;
	public abstract XDMDocument storeDocumentFromString(XDMDocumentId docId, String xml, Properties props) throws XDMException;
	
	public Source getDocumentAsSource(XDMDocumentId docId) throws XDMException {
		String xml = getDocumentAsString(docId);
		if (xml != null) {
			return new StreamSource(new StringReader(xml));
		}
		return null;
	}
	
	public InputStream getDocumentAsSream(XDMDocumentId docId) throws XDMException {
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
	
	public XDMDocument storeDocumentFromSource(XDMDocumentId docId, Source source, Properties props) throws XDMException {
		try {
			// TODO: get serialization props only..
			String xml = XMLUtils.sourceToString(source, null);
			return storeDocumentFromString(docId, xml, props);
		} catch (IOException ex) {
			logger.error("storeDocumentFromSource.error", ex);
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}
	
	public XDMDocument storeDocumentFromStream(XDMDocumentId docId, InputStream stream, Properties props) throws XDMException {
		try {
			String xml = XMLUtils.textToString(stream);
			return storeDocumentFromString(docId, xml, props);
		} catch (IOException ex) {
			logger.error("storeDocumentFromStream.error", ex);
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}

}
