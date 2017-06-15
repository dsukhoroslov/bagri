package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.mt_xml;
import static com.bagri.core.Constants.pn_document_data_format;

import java.io.StringReader;
import java.util.Properties;

import javax.xml.transform.stream.StreamSource;

import com.bagri.core.api.BagriException;
import com.bagri.core.server.api.DocumentManagement;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;

public class XmlResourceImpl extends BaseResourceImpl {

	private String xml;
	
	public XmlResourceImpl(DocumentManagement docMgr, long docKey) {
		super(docMgr, docKey);
	}

	@Override
	public Item getItem(XPathContext context) throws XPathException {
		if (xml == null) {
			Properties props = new Properties();
			props.setProperty(pn_document_data_format, "XML");
			try {
				xml = docMgr.getDocumentAsString(docKey, props);
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
