package com.bagri.xquery.saxon.ext.util;

import static com.bagri.core.Constants.bg_ns;
import static com.bagri.core.Constants.bg_schema;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;

public class HashCode extends ExtensionFunctionDefinition {

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(bg_schema, bg_ns, "hash-code");
	}
	
	@Override 
	public int getMinimumNumberOfArguments() { 
		return 1; 
	}
	
	@Override 
	public int getMaximumNumberOfArguments() { 
		return 1; 
	} 	

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {BuiltInAtomicType.ANY_ATOMIC.one()}; 
	}
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_INTEGER; 
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				//return Int64Value.makeIntegerValue(SequenceTool.convertToJava(arguments[0].head()).hashCode());
				return new Int64Value(SequenceTool.convertToJava(arguments[0].head()).hashCode());
			}
		};
	} 
	
}


