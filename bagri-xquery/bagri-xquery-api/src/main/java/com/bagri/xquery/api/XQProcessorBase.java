package com.bagri.xquery.api;

import com.bagri.xdm.access.api.XDMDocumentManagement;

public abstract class XQProcessorBase {

    private XDMDocumentManagement dMgr;

    public XDMDocumentManagement getXdmManager() {
    	return this.dMgr;
    }

    public void setXdmManager(XDMDocumentManagement mgr) {
    	//config.setConfigurationProperty("xdm", mgr);
    	this.dMgr = mgr;
    }
    
}
