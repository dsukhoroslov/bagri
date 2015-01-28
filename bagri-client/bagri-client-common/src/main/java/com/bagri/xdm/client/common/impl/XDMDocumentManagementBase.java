package com.bagri.xdm.client.common.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xquery.XQException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xqj.BagriXQUtils;

public abstract class XDMDocumentManagementBase {
	
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	public abstract String getDocumentAsString(long docId);
	public abstract XDMDocument storeDocumentFromString(long docId, String uri, String xml);
	
	public Source getDocumentAsSource(long docId) {
		String xml = getDocumentAsString(docId);
		if (xml != null) {
			return new StreamSource(new StringReader(xml));
		}
		return null;
	}
	
	public InputStream getDocumentAsSream(long docId) {
		String xml = getDocumentAsString(docId);
		if (xml != null) {
			try {
				return new ByteArrayInputStream(xml.getBytes("utf-8"));
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex);
			}
		}
		return null;
	}
	
	public XDMDocument storeDocumentFromSource(long docId, String uri, Source source) {
		try {
			String xml = BagriXQUtils.sourceToString(source);
			return storeDocumentFromString(docId, uri, xml);
		} catch (XQException ex) {
			logger.error("storeDocumentFromSource.error; " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}
	
	public XDMDocument storeDocumentFromStream(long docId, String uri, InputStream stream) {
		try {
			// refactor the method below..
			String xml = BagriXQUtils.textToString(stream);
			return storeDocumentFromString(docId, uri, xml);
		} catch (XQException ex) {
			throw new RuntimeException(ex);
		}
	}

}
