package com.bagri.xquery.saxon.ext.doc;

import static com.bagri.core.Constants.cmd_remove_document;
import static com.bagri.xquery.saxon.SaxonUtils.SINGLE_ANY_URI;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.BagriException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.SequenceType;

public class RemoveDocument extends DocumentFunctionExtension {
	
	public RemoveDocument(DocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return cmd_remove_document;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SINGLE_ANY_URI}; 
	}

	@Override 
	public int getMaximumNumberOfArguments() { 
		return 1; 
	} 	
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SINGLE_ANY_URI;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				try {
					String uri = arguments[0].head().getStringValue();
					xdm.removeDocument(uri);
					return new AnyURIValue(uri);
				} catch (BagriException ex) {
					throw new XPathException(ex);
				}
			}
        };
	} 
}


