package com.bagri.xquery.saxon.ext.util;

import static com.bagri.core.Constants.bg_ns;
import static com.bagri.core.Constants.bg_schema;

import java.util.UUID;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class GetUuid extends ExtensionFunctionDefinition {

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(bg_schema, bg_ns, "get-uuid");
	}
	
	@Override 
	public int getMinimumNumberOfArguments() { 
		return 0; 
	}
	
	@Override 
	public int getMaximumNumberOfArguments() { 
		return 2; 
	} 	

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {BuiltInAtomicType.INT.zeroOrOne(), BuiltInAtomicType.INT.zeroOrOne()}; 
	}
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_STRING; 
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				if (arguments.length > 0) {
					long most = (Long) SequenceTool.convertToJava(arguments[0].head());
					long least = 0;
					if (arguments.length > 1) {
						least = (Long) SequenceTool.convertToJava(arguments[1].head());
					}
					return StringValue.makeStringValue(new UUID(most, least).toString());
				}
				return StringValue.makeStringValue(UUID.randomUUID().toString());
			}
		};
	} 
	
}
