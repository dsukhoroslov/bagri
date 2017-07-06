package com.bagri.xquery.saxon.ext.doc;

import static com.bagri.core.Constants.cmd_store_document;
import static com.bagri.xquery.saxon.SaxonUtils.sequence2Properties;

import java.util.Properties;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.Document;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.SequenceType;

public class StoreDocument extends DocumentFunctionExtension {

	//private static final Logger logger = LoggerFactory.getLogger(StoreDocument.class);
	
	public StoreDocument(DocumentManagement xdm) {
		super(xdm);
	}

	@Override
	public String getFunctionName() {
		return cmd_store_document;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {BuiltInAtomicType.ANY_URI.one(), SequenceType.SINGLE_STRING, SequenceType.ATOMIC_SEQUENCE}; //STRING_SEQUENCE};
	}
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return BuiltInAtomicType.ANY_URI.one();
	}

	@Override 
	public int getMinimumNumberOfArguments() { 
		return 2; 
	}
	
	@Override 
	public int getMaximumNumberOfArguments() { 
		return 3; 
	} 	
	
	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				String uri = arguments[0].head().getStringValue();
				String xml = arguments[1].head().getStringValue();
				Properties props = null; 
				if (arguments.length > 2) {
					props = sequence2Properties(arguments[2]);
				}
				try {
					Document doc = xdm.storeDocumentFromString(uri, xml, props);
					//HashTrieMap map = new HashTrieMap(context);
					return new AnyURIValue(doc.getUri());
				} catch (BagriException ex) {
					throw new XPathException(ex);
				}
			}

		};
	} 
	
}
