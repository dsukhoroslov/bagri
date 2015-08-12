package com.bagri.xquery.saxon.extension;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.XDMDocument;

public class StoreDocumentWithId extends DocumentFunctionExtension {
	
	public StoreDocumentWithId(XDMDocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return "store-document";
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.SINGLE_STRING, SequenceType.OPTIONAL_NUMERIC};
	}
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_STRING;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		//"declare option bgdm:document-format \"JSON\";\n\n" +

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				String xml = arguments[0].head().getStringValue();
				long docId = 0;
				if (arguments.length > 1) {
					docId = Long.parseLong(arguments[1].head().getStringValue());
				}

				try {
					// validate document ?
					XDMDocument doc = xdm.storeDocumentFromString(docId, null, xml);
					//return new Int64Value(doc.getDocumentId());
					return new StringValue(doc.getUri());
				} catch (XDMException ex) {
					throw new XPathException(ex);
				}
			}

			// think to override it, get ctx and take document-format option from it somehow
			//this.supplyStaticContext(context, locationId, arguments);
			
		};
	} 
}
