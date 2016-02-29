package com.bagri.xquery.saxon.extension;

import static com.bagri.xdm.common.XDMConstants.cmd_remove_cln_documents;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

public class RemoveCollectionDocuments extends DocumentFunctionExtension {
	
	public RemoveCollectionDocuments(XDMDocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return cmd_remove_cln_documents;
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
		return SequenceType.EMPTY_SEQUENCE; //SINGLE_INT;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				// TODO: get clnId from args
				int clnId = 0;
				try {
					xdm.removeCollectionDocuments(clnId);
				} catch (XDMException ex) {
					throw new XPathException(ex);
				}
				return null; //new Int64Value(doc.getDocumentId());
			}
        };
	} 
}



