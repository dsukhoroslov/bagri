package com.bagri.xquery.saxon.ext.doc;

import static com.bagri.core.Constants.cmd_remove_cln_documents;
import static com.bagri.xquery.saxon.SaxonUtils.sequence2Properties;

import java.util.Properties;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.BagriException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

public class RemoveDocuments extends DocumentFunctionExtension {
	
	public RemoveDocuments(DocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return cmd_remove_cln_documents;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.SINGLE_STRING, MapType.OPTIONAL_MAP_ITEM}; 
	}

	@Override 
	public int getMaximumNumberOfArguments() { 
		return 2; 
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
				
				String pattern = arguments[0].head().getStringValue();
				Properties props = null; 
				if (arguments.length > 1) {
					props = sequence2Properties(arguments[1]);
				}
				try {
					xdm.removeDocuments(pattern, props);
				} catch (BagriException ex) {
					throw new XPathException(ex);
				}
				return EmptySequence.getInstance(); 
			}
        };
	} 
}



