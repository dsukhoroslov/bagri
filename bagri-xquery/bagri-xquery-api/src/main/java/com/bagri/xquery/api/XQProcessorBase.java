package com.bagri.xquery.api;

import javax.validation.constraints.NotNull;
import javax.xml.xquery.XQDataFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMRepository;

public abstract class XQProcessorBase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private XQDataFactory xqFactory;
    private XDMRepository xRepo;

    public XDMRepository getRepository() {
    	return xRepo;
    }
    
    public XDMQueryManagement getQueryManagement() {
    	return xRepo.getQueryManagement();
    }

    public XQDataFactory getXQDataFactory() {
    	return xqFactory;
    }

    public void setRepository(@NotNull XDMRepository xRepo) {
    	//config.setConfigurationProperty("xdm", mgr);
    	this.xRepo = xRepo;
    	logger.trace("setRepository; got Repo: {}", xRepo); 
    }
    
    //@Override
    public void setXQDataFactory(@NotNull XQDataFactory xqFactory) {
    	this.xqFactory = xqFactory;
    }
    
}
