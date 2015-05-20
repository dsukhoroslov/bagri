package com.bagri.xquery.saxon.extension;

import com.bagri.xdm.system.XDMFunction;
import com.bagri.xdm.system.XDMParameter;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

public class StaticFunctionExtension extends ExtensionFunctionDefinition {
	
	private XDMFunction xdf;
	
	public StaticFunctionExtension() {
		// des-serialization ?
	}
	
	public StaticFunctionExtension(XDMFunction xdf) {
		this.xdf = xdf;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName("java", "", xdf.getMethod());
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		SequenceType[] result = new SequenceType[xdf.getParameters().size()];
		for (XDMParameter xdp: xdf.getParameters()) {
			//
		}
		return result;
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return null;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				return null;
			}
		};
		
	}

}
