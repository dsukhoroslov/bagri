package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.bg_schema;

import java.util.Map;

import com.bagri.core.api.BagriException;
import com.bagri.core.server.api.DocumentManagement;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;

public class MapResourceImpl implements Resource {
	
	private Configuration config;
	private DocumentManagement docMgr;
	private long docKey;
	
	public MapResourceImpl(Configuration config, DocumentManagement docMgr, long docKey) {
		this.config = config;
		this.docMgr = docMgr;
		this.docKey = docKey;
	}

	@Override
	public String getResourceURI() {
		return bg_schema + ":/" + docKey;
	}

	@Override
	public Item getItem(XPathContext context) throws XPathException {
		try {
			Map<String, Object> map = docMgr.getDocumentAsMap(docKey, null);
        	return new MapItemImpl(map, config);
		} catch (BagriException ex) {
			throw new XPathException(ex);
		}
	}

	@Override
	public String getContentType() {
		return null;
	}

}
