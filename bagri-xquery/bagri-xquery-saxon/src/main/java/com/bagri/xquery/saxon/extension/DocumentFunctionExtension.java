package com.bagri.xquery.saxon.extension;

import static com.bagri.xdm.common.XDMConstants.bg_ns;
import static com.bagri.xdm.common.XDMConstants.bg_schema;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;

import com.bagri.xdm.api.XDMDocumentManagement;

public abstract class DocumentFunctionExtension extends ExtensionFunctionDefinition {

	protected XDMDocumentManagement xdm;
	
	public DocumentFunctionExtension(XDMDocumentManagement xdm) {
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

	
}
