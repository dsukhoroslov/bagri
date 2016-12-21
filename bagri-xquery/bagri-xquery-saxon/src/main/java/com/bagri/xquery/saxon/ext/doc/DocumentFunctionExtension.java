package com.bagri.xquery.saxon.ext.doc;

import static com.bagri.core.Constants.bg_ns;
import static com.bagri.core.Constants.bg_schema;

import java.util.Properties;

import com.bagri.core.api.DocumentManagement;

import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;

public abstract class DocumentFunctionExtension extends ExtensionFunctionDefinition {

	protected DocumentManagement xdm;
	
	public DocumentFunctionExtension(DocumentManagement xdm) {
		this.xdm = xdm;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName(bg_schema, bg_ns, getFunctionName());
	}
	
	protected abstract String getFunctionName();

	@Override 
	public int getMinimumNumberOfArguments() { 
		return 1; 
	}
	
	@Override 
	public int getMaximumNumberOfArguments() {
		return 2; 
	} 	

	@Override 
	public boolean hasSideEffects() {
		return true; 
	} 	
	
}
