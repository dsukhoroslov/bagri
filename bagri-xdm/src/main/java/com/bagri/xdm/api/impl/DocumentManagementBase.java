package com.bagri.xdm.api.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bagri.common.util.FileUtils.def_encoding;

import com.bagri.common.util.XMLUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.Document;

/**
 * Base implementation for XDM Document Management interface. SEveral common methods implemented 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class DocumentManagementBase {
	
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 
	 * @param uri the XDM document uri
	 * @return XDM Document content as a plain text
	 * @throws XDMException in case of any error
	 */
	public abstract String getDocumentAsString(String uri) throws XDMException;
	
	
	/**
	 * Creates a new Document or overrides an existing one in XDM repository
	 * 
	 * @param uri String; the XDM document uri  
	 * @param content document's text (JSON, XML, ..) representation, can not be null
	 * @param props Properties; the document processing instructions
	 * @return XDMDocument created or overridden (versioned) document
	 * @throws XDMException in case of any error
	 */
	public abstract Document storeDocumentFromString(String uri, String content, Properties props) throws XDMException;

	/**
	 * constructs {@link InputStream} over XDMDocument content identified by the uri provided 
	 * 
	 * @param uri the XDM document uri
	 * @return {@link InputStream} over the document's content
	 * @throws XDMException in case of any error
	 */
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
	
	/**
	 * constructs new XDMDocument or overrides an existing one in XDM repository
	 * 
	 * @param uri String; the XDM document uri  
	 * @param stream the {@link InputStream} over document's text (JSON, XML, ..) representation, can not be null
	 * @param props Properties; the document processing instructions
	 * @return XDMDocument created or overridden (versioned) document
	 * @throws XDMException in case of any error
	 */
	public Document storeDocumentFromStream(String uri, InputStream stream, Properties props) throws XDMException {
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
