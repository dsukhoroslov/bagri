package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.pn_document_data_format;

import java.util.Map;
import java.util.Properties;

import com.bagri.core.api.BagriException;
import com.bagri.core.server.api.DocumentManagement;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;

public class MapResourceImpl extends ResourceImplBase {
	
	private Map<String, Object> map;
	
	public MapResourceImpl(DocumentManagement docMgr, long docKey) {
		super(docMgr, docKey);
	}

	@Override
	public Item getItem(XPathContext context) throws XPathException {
		if (map == null) {
			Properties props = new Properties();
			props.setProperty(pn_document_data_format, "MAP");
			try {
				map = docMgr.getDocumentAs(docKey, props);
			} catch (BagriException ex) {
				throw new XPathException(ex);
			}
		}
		if (map != null) {
    		return new MapItemImpl(map, context.getConfiguration());
		}
		return null;
	}

}
