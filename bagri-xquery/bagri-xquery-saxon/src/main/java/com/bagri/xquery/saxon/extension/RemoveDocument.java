package com.bagri.xquery.saxon.extension;

import static com.bagri.xdm.common.XDMConstants.cmd_remove_document;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;

public class RemoveDocument extends DocumentFunctionExtension {
	
	public RemoveDocument(XDMDocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return cmd_remove_document;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.SINGLE_ANY_URI}; 
	}

	@Override 
	public int getMaximumNumberOfArguments() { 
		return 1; 
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
				
				try {
					xdm.removeDocument(toUri(arguments[0]));
				} catch (XDMException ex) {
					throw new XPathException(ex);
				}
				return EmptySequence.getInstance();
			}
        };
	} 
}


