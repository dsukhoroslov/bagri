package com.bagri.xquery.saxon.ext.doc;

import static com.bagri.core.Constants.cmd_get_document;
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
import net.sf.saxon.value.StringValue;

public class GetDocumentContent extends DocumentFunctionExtension {
	
	public GetDocumentContent(DocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return cmd_get_document;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.SINGLE_ANY_URI, MapType.OPTIONAL_MAP_ITEM}; 
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
					String uri = arguments[0].head().getStringValue();
					Properties props = null; 
					if (arguments.length > 1) {
						props = sequence2Properties(arguments[1]);
					}
					result = xdm.getDocumentAsString(uri, props);
				} catch (BagriException ex) {
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



