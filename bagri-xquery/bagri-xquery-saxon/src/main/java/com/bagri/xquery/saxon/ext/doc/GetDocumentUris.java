package com.bagri.xquery.saxon.ext.doc;

import static com.bagri.core.Constants.cmd_get_document_uris;

import java.util.ArrayList;
import java.util.Collection;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentManagement;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class GetDocumentUris extends DocumentFunctionExtension {
	
	public GetDocumentUris(DocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return cmd_get_document_uris;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.SINGLE_STRING}; 
	}

	@Override 
	public int getMaximumNumberOfArguments() { 
		return 1; 
	} 	
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.STRING_SEQUENCE; 
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				String pattern = arguments[0].head().getStringValue();
				try {
					Collection<String> uris = xdm.getDocumentUris(pattern);
					ArrayList<AtomicValue> list = new ArrayList<>(uris.size());
					for (String uri: uris) {
						list.add(new StringValue(uri));
					}
					return new AtomicArray(list);
				} catch (BagriException ex) {
					throw new XPathException(ex);
				}
			}
        };
	} 

}
