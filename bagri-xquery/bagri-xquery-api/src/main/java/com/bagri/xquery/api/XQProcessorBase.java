package com.bagri.xquery.api;

import javax.xml.xquery.XQDataFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMQueryManagement;

public abstract class XQProcessorBase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private XQDataFactory xqFactory;
    private XDMDocumentManagement dMgr;
    private XDMQueryManagement qMgr;

    public XDMDocumentManagement getXdmManager() {
    	return dMgr;
    }
    
    public XQDataFactory getXQDataFactory() {
    	return xqFactory;
    }

    public void setXdmManager(XDMDocumentManagement mgr) {
    	//config.setConfigurationProperty("xdm", mgr);
    	this.dMgr = mgr;
    	logger.trace("setXdmManager; got XDM: {}", dMgr); 
    }
    
    //@Override
    public void setXQDataFactory(XQDataFactory xqFactory) {
    	this.xqFactory = xqFactory;
    }
    
    public XDMQueryManagement getXQManager() {
    	return qMgr;
    }
    
    public void setXQManager(XDMQueryManagement mgr) {
    	this.qMgr = mgr;
    	logger.trace("setXQManager; got XQM: {}", qMgr); 
    }
    
}
