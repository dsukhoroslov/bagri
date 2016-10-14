package com.bagri.xquery.saxon.ext.doc;

import static com.bagri.xdm.common.Constants.bg_ns;
import static com.bagri.xdm.common.Constants.bg_schema;
import static com.bagri.xquery.saxon.SaxonUtils.sequence2Properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.bagri.xdm.api.QueryManagement;
import com.bagri.xdm.api.XDMException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class QueryDocumentUris extends ExtensionFunctionDefinition {

	protected QueryManagement qm;
	
	public QueryDocumentUris(QueryManagement qm) {
		this.qm = qm;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(bg_schema, bg_ns, "query-document-uris");
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
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.SINGLE_STRING, SequenceType.ANY_SEQUENCE, SequenceType.ATOMIC_SEQUENCE}; 
	}
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.STRING_SEQUENCE;
	}

	public static Map<String, Object> sequence2Params(Sequence sq) throws XPathException {
		SequenceIterator itr = sq.iterate();
		Map<String, Object> params = new HashMap<>();
		do {
			Item item = itr.next();
			if (item != null) {
				String name = item.getStringValue();
				Object value = null;
				item = itr.next();
				if (item != null) {
					value = SequenceTool.convertToJava(item);
				}
				params.put(name, value);
			} else {
				break;
			}
		} while (true);
		return params;
	}
	
	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				String query = arguments[0].head().getStringValue();
				Map<String, Object> params = sequence2Params(arguments[1]);
				Properties props = null; 
				if (arguments.length > 2) {
					props = sequence2Properties(arguments[2]);
				}
				try {
					Collection<String> uris = qm.getDocumentUris(query, params, props);
					ArrayList<AtomicValue> list = new ArrayList<>(uris.size());
					for (String uri: uris) {
						list.add(new StringValue(uri));
					}
					return new AtomicArray(list);
				} catch (XDMException ex) {
					throw new XPathException(ex);
				}
			}

		};
	} 

}
