package com.bagri.xdm.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.common.XDMFactory;

public abstract class XDMDocumentManagementBase {
	
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	protected XDMFactory mFactory;
	protected XDMSchemaDictionary mDictionary;
	
	public XDMFactory getXdmFactory() {
		return this.mFactory;
	}
	
	public void setXdmFactory(XDMFactory factory) {
		this.mFactory = factory;
	}
	
	//@Override
	public XDMSchemaDictionary getSchemaDictionary() {
		return this.mDictionary;
	}
	
	public void setSchemaDictionary(XDMSchemaDictionary dictionary) {
		this.mDictionary = dictionary;
	}


}
