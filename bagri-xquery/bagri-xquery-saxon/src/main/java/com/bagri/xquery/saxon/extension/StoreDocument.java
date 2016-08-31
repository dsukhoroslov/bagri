package com.bagri.xquery.saxon.extension;

import static com.bagri.xdm.common.Constants.cmd_store_document;

import java.util.Properties;

import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.Document;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;

public class StoreDocument extends DocumentFunctionExtension {

	public StoreDocument(DocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return cmd_store_document;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.SINGLE_ANY_URI, SequenceType.SINGLE_STRING, SequenceType.ATOMIC_SEQUENCE}; //STRING_SEQUENCE};
	}
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_NUMERIC;
	}

	@Override 
	public int getMinimumNumberOfArguments() { 
		return 2; 
	}
	
	@Override 
	public int getMaximumNumberOfArguments() { 
		return 3; 
	} 	
	
	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				String uri = toUri(arguments[0]);
				String xml = arguments[1].head().getStringValue();
				Properties props = null; 
				if (arguments.length > 2) {
					props = toProperties(arguments[2]);
				}
				try {
					Document doc = xdm.storeDocumentFromString(uri, xml, props);
					return new Int64Value(doc.getDocumentKey());
					//return new ObjectValue(doc);
				} catch (XDMException ex) {
					throw new XPathException(ex);
				}
			}

		};
	} 
	
}
