package com.bagri.xquery.saxon.extension;

import static com.bagri.xdm.common.XDMConstants.cmd_store_document;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;

public class StoreDocument extends DocumentFunctionExtension {

	//private static final Logger logger = LoggerFactory.getLogger(StoreDocument.class);
	
	public StoreDocument(XDMDocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return cmd_store_document;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.SINGLE_STRING, SequenceType.ATOMIC_SEQUENCE, SequenceType.STRING_SEQUENCE};
	}
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_NUMERIC;
	}

	@Override 
	public int getMaximumNumberOfArguments() { 
		return 3; 
	} 	
	
	@Override
	public ExtensionFunctionCall makeCallExpression() {

		//"declare option bgdm:document-format \"JSON\";\n\n" +

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				String xml = arguments[0].head().getStringValue();
				XDMDocumentId docId = null;
				if (arguments.length > 1) {
					docId = toDocumentId(arguments[1]);
				}
				Properties props = null; 
				if (arguments.length > 2) {
					props = toProperties(arguments[2]);
				}
				try {
					XDMDocument doc = xdm.storeDocumentFromString(docId, xml, props);
					return new Int64Value(doc.getDocumentKey());
					//return new ObjectValue(doc);
				} catch (XDMException ex) {
					throw new XPathException(ex);
				}
			}

		};
	} 
	
}
