package com.bagri.xquery.saxon.ext.util;

import static com.bagri.xdm.common.Constants.bg_ns;
import static com.bagri.xdm.common.Constants.bg_schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

public class LogOutput extends ExtensionFunctionDefinition {

    private static final transient Logger logger = LoggerFactory.getLogger("com.bagri.XQuery.Output");
    
	//public LogOutput() {
	//}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(bg_schema, bg_ns, "log-output");
	}
	
	@Override 
	public int getMinimumNumberOfArguments() { 
		return 1; 
	}
	
	@Override 
	public int getMaximumNumberOfArguments() { 
		return 3; 
	} 	

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.SINGLE_STRING, SequenceType.OPTIONAL_STRING, SequenceType.OPTIONAL_STRING}; 
	}
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.VOID; // EMPTY_SEQUENCE
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				logger.info("call.enter; arguments: {}", (Object[]) arguments); 
				String message = arguments[0].head().getStringValue();
				String level = "debug";
				if (arguments.length > 1) {
					level = arguments[1].head().getStringValue();
				}
				Logger log = logger;
				if (arguments.length > 2) {
					String l = arguments[2].head().getStringValue();
					log = LoggerFactory.getLogger(l);
				}
				logger.info("call; message: {}; level: {}; logger: {}", message, level, log); 
				switch (level) {
					case "trace": log.trace(message); break;
					case "debug": log.debug(message); break;
					case "info": log.info(message); break;
					case "warn": log.warn(message); break;
					case "error": log.error(message);
				}
				return null;
			}

		};
	} 
	

}
