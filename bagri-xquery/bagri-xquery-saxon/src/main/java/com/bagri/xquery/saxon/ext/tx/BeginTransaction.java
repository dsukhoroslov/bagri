package com.bagri.xquery.saxon.ext.tx;

import static com.bagri.core.Constants.bg_ns;
import static com.bagri.core.Constants.bg_schema;

import java.util.UUID;

import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.api.TransactionManagement;
import com.bagri.core.api.BagriException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;

public class BeginTransaction extends ExtensionFunctionDefinition {
	
	private TransactionManagement txMgr;
	
	public BeginTransaction(TransactionManagement txMgr) {
		this.txMgr = txMgr;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(bg_schema, bg_ns, "begin-transaction");
	}
	
	@Override 
	public int getMinimumNumberOfArguments() { 
		return 0; 
	}
	
	@Override 
	public int getMaximumNumberOfArguments() { 
		return 1; 
	} 	

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.OPTIONAL_STRING}; 
	}
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_INT; 
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				long txId;
				try {
					if (arguments.length > 0) {
						txId = txMgr.beginTransaction(TransactionIsolation.valueOf(arguments[0].head().getStringValue()));
					} else {
						txId = txMgr.beginTransaction(); 
					}
					return new Int64Value(txId);
				} catch (BagriException ex) {
					throw new XPathException(ex);
				}
			}
		};
	} 
	
}
