package com.bagri.core.xquery.api;

import javax.xml.xquery.XQDataFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.QueryManagement;
import com.bagri.core.api.SchemaRepository;

/**
 * Base abstract XQ Processor implementation, shared between client and server processor implementations.
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class XQProcessorBase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private XQDataFactory xqFactory;
    private SchemaRepository xRepo;

    /**
     * 
     * @return XDM repository
     */
    public SchemaRepository getRepository() {
    	return xRepo;
    }

    /**
     * 
     * @return document management implementation
     */
    public DocumentManagement getDocumentManagement() {
    	return xRepo.getDocumentManagement();
    }

    /**
     * 
     * @return query management implementation
     */
    public QueryManagement getQueryManagement() {
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
    public void setRepository(SchemaRepository xRepo) {
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
