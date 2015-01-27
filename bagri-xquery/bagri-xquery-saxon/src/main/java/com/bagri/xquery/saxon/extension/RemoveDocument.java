package com.bagri.xquery.saxon.extension;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.domain.XDMDocument;

import static com.bagri.xqj.BagriXQConstants.bg_ns;
import static com.bagri.xqj.BagriXQConstants.bg_schema;

public class RemoveDocument extends ExtensionFunctionDefinition {
	
	private XDMDocumentManagement xdm;
	
	public RemoveDocument(XDMDocumentManagement xdm) {
		this.xdm = xdm;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(bg_schema, bg_ns, "remove-document");
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.OPTIONAL_NUMERIC, SequenceType.OPTIONAL_NUMERIC}; // or string ??
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


