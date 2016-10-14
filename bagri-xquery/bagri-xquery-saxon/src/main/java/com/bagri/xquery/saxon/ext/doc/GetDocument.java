package com.bagri.xquery.saxon.ext.doc;

import static com.bagri.xdm.common.Constants.cmd_get_document;

import java.util.Properties;

import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.XDMException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class GetDocument extends DocumentFunctionExtension {
	
	public GetDocument(DocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return cmd_get_document;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.SINGLE_ANY_URI, SequenceType.STRING_SEQUENCE}; 
	}

	@Override 
	public int getMaximumNumberOfArguments() { 
		return 2; 
	} 	
	
	@Override 
	public boolean hasSideEffects() {
		return false; 
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
					String uri = toUri(arguments[0]);
					Properties props = null; 
					if (arguments.length > 1) {
						props = toProperties(arguments[1]);
					}
					result = xdm.getDocumentAsString(uri, props);
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



