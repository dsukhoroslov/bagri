package com.bagri.xquery.saxon.ext.tx;

import static com.bagri.xdm.common.Constants.bg_ns;
import static com.bagri.xdm.common.Constants.bg_schema;

import com.bagri.xdm.api.TransactionManagement;
import com.bagri.xdm.api.XDMException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

public class CommitTransaction extends ExtensionFunctionDefinition {
	
	private TransactionManagement txMgr;
	
	public CommitTransaction(TransactionManagement txMgr) {
		this.txMgr = txMgr;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(bg_schema, bg_ns, "commit-transaction");
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
		return new SequenceType[] {SequenceType.SINGLE_INT}; 
	}
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.VOID; 
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				long txId = (Long) SequenceTool.convertToJava(arguments[0].head());
				try {
					txMgr.commitTransaction(txId);
					return null;
				} catch (XDMException ex) {
					throw new XPathException(ex);
				}
			}
		};
	} 
	
}
