package com.bagri.xquery.saxon.extension;

import static com.bagri.xdm.common.XDMConstants.bg_ns;
import static com.bagri.xdm.common.XDMConstants.bg_schema;

import java.util.Properties;

import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.StringValue;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.common.XDMDocumentId;

public abstract class DocumentFunctionExtension extends ExtensionFunctionDefinition {

	protected XDMDocumentManagement xdm;
	
	public DocumentFunctionExtension(XDMDocumentManagement xdm) {
		this.xdm = xdm;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(bg_schema, bg_ns, getFunctionName());
	}
	
	protected abstract String getFunctionName();

	@Override 
	public int getMinimumNumberOfArguments() { 
		return 1; 
	}
	
	@Override 
	public int getMaximumNumberOfArguments() { 
		return 2; 
	} 	

	protected XDMDocumentId toDocumentId(Sequence ids) throws XPathException {
		SequenceIterator itr = ids.iterate();
		Item id = itr.next();
		if (id instanceof StringValue) {
			return new XDMDocumentId(id.getStringValue());
		}
		long docKey = Long.parseLong(id.getStringValue());
		id = itr.next();
		if (id == null) {
			return new XDMDocumentId(docKey);
		}
		if (id instanceof StringValue) {
			return new XDMDocumentId(docKey, id.getStringValue());
		}
		int version = Integer.parseInt(id.getStringValue());
		id = itr.next();
		if (id == null) {
			return new XDMDocumentId(docKey, version);
		}
		return new XDMDocumentId(docKey, version, id.getStringValue());
	}
	
	protected Properties toProperties(Sequence sq) throws XPathException {
		SequenceIterator itr = sq.iterate();
		Properties props = new Properties();
		do {
			Item item = itr.next();
			if (item != null) {
				String prop = item.getStringValue();
				int pos = prop.indexOf("=");
				if (pos > 0) {
					props.setProperty(prop.substring(0, pos), prop.substring(pos + 1));
				}
			} else {
				break;
			}
		} while (true);
		return props;
	}
	
	
}
