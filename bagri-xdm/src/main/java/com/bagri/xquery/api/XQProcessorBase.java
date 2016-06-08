package com.bagri.xquery.api;

import javax.xml.xquery.XQDataFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMRepository;

/**
 * Base abstract XQ Processor implementation, shared between client and server processor implementations.
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class XQProcessorBase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private XQDataFactory xqFactory;
    private XDMRepository xRepo;

    /**
     * 
     * @return XDM repository
     */
    public XDMRepository getRepository() {
    	return xRepo;
    }

    /**
     * 
     * @return document management implementation
     */
    public XDMDocumentManagement getDocumentManagement() {
    	return xRepo.getDocumentManagement();
    }

    /**
     * 
     * @return query management implementation
     */
    public XDMQueryManagement getQueryManagement() {
    	return xRepo.getQueryManagement();
    }

    /**
     * 
     * @return assigned XQJ data factory
     */
    public XQDataFactory getXQDataFactory() {
    	return xqFactory;
    }

    /**
     * 
     * @param xRepo the XDM repository to assign with this XQ processor
     */
    public void setRepository(XDMRepository xRepo) {
    	this.xRepo = xRepo;
    	logger.trace("setRepository; got Repo: {}", xRepo); 
    }
    
    /**
     * 
     * @param xqFactory the XQJ data factory to assign with this XQ processor
     */
    public void setXQDataFactory(XQDataFactory xqFactory) {
    	this.xqFactory = xqFactory;
    }
    
}
