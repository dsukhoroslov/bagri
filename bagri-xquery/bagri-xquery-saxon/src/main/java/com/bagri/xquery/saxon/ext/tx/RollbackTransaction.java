package com.bagri.xquery.saxon.ext.tx;

import static com.bagri.core.Constants.bg_ns;
import static com.bagri.core.Constants.bg_schema;

import com.bagri.core.api.TransactionManagement;
import com.bagri.core.api.BagriException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.SequenceType;

public class RollbackTransaction extends ExtensionFunctionDefinition {
	
	private TransactionManagement txMgr;
	
	public RollbackTransaction(TransactionManagement txMgr) {
		this.txMgr = txMgr;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(bg_schema, bg_ns, "rollback-transaction");
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
		return new SequenceType[] {BuiltInAtomicType.INT.one()}; 
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
					txMgr.rollbackTransaction(txId);
					return null;
				} catch (BagriException ex) {
					throw new XPathException(ex);
				}
			}
		};
	} 

}
