package com.bagri.xquery.saxon;

import java.util.Map;

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
			try {
				map = docMgr.getDocumentAsMap(docKey, null);
			} catch (BagriException ex) {
				throw new XPathException(ex);
			}
		}
    	return new MapItemImpl(map, context.getConfiguration());
	}

}
