package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.mt_xml;
import static com.bagri.core.Constants.pn_document_data_format;
import static com.bagri.core.Constants.pn_document_headers;

import java.io.StringReader;
import java.util.Properties;

import javax.xml.transform.stream.StreamSource;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.server.api.DocumentManagement;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;

public class XmlResourceImpl extends ResourceImplBase {

	private String xml;
	
	public XmlResourceImpl(DocumentManagement docMgr, long docKey) {
		super(docMgr, docKey);
	}

	@Override
	public Item getItem(XPathContext context) throws XPathException {
		if (xml == null) {
			Properties props = new Properties();
			props.setProperty(pn_document_data_format, "XML");
			props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CONTENT));
			try {
				DocumentAccessor doc = docMgr.getDocument(docKey, props);
				if (doc == null) {
					throw new XPathException("Lost document for key: " + docKey);
				}
				xml = doc.getContent();
			} catch (BagriException ex) {
				throw new XPathException(ex);
			}
		}
		StreamSource ss = new StreamSource(new StringReader(xml));
		return context.getConfiguration().buildDocumentTree(ss).getRootNode();
	}

	@Override
	public String getContentType() {
		return mt_xml;
	}

}
