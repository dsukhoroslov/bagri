package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.mt_json;
import static com.bagri.core.Constants.pn_document_data_format;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.bagri.core.api.BagriException;
import com.bagri.core.server.api.DocumentManagement;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.ma.json.ParseJsonFn;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;

public class JsonResourceImpl extends ResourceImplBase {
	
	private String json; 

	public JsonResourceImpl(DocumentManagement docMgr, long docKey) {
		super(docMgr, docKey);
	}

	@Override
	public Item getItem(XPathContext context) throws XPathException {
		if (json == null) {
			Properties props = new Properties();
			props.setProperty(pn_document_data_format, "JSON");
			try {
				json = docMgr.getDocumentAsString(docKey, props);
			} catch (BagriException ex) {
				throw new XPathException(ex);
			}
		}
        //MapItem options = new HashTrieMap(context);
		Map<String, Sequence> options = new HashMap<>();
        return ParseJsonFn.parse(json, options, context);
	}

	@Override
	public String getContentType() {
		return mt_json;
	}

}
