package com.bagri.xquery.saxon.extension;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

import com.bagri.xdm.api.XDMDocumentManagement;

public class RemoveDocument extends DocumentFunctionExtension {
	
	public RemoveDocument(XDMDocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return "remove-document";
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.SINGLE_NUMERIC, SequenceType.OPTIONAL_NUMERIC}; // or string ??
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.EMPTY_SEQUENCE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				String docId = arguments[0].head().getStringValue();
				xdm.removeDocument(Long.parseLong(docId));
				return null; //new Int64Value(doc.getDocumentId());
			}
        };
	} 
}


