package com.bagri.xquery.saxon.extension;

import static com.bagri.xdm.common.XDMConstants.cmd_get_document;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class GetDocument extends DocumentFunctionExtension {
	
	public GetDocument(XDMDocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return cmd_get_document;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.ATOMIC_SEQUENCE}; 
	}

	@Override 
	public int getMaximumNumberOfArguments() { 
		return 1; 
	} 	
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.OPTIONAL_STRING;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {

				String result = null;
				try {
					result = xdm.getDocumentAsString(toUri(arguments[0]));
				} catch (XDMException ex) {
					throw new XPathException(ex);
				}
				if (result == null) {
					return EmptySequence.getInstance();
				}
				return new StringValue(result);
			}
        };
	} 
}



