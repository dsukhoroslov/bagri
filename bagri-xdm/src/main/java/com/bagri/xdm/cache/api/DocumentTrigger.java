package com.bagri.xdm.cache.api;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.domain.Document;

/**
 * Represents trigger to run at various points of document life cycle 
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface DocumentTrigger {

	/**
	 * fires before document inserted into XDM Schema
	 * 
	 * @param doc the XDM document
	 * @param repo the XDM Schema Repository
	 * @throws XDMException in case of processing error
	 */
	void beforeInsert(Document doc, SchemaRepository repo) throws XDMException;
	
	/**
	 * fires after document inserted into XDM Schema
	 * 
	 * @param doc the XDM document
	 * @param repo the XDM Schema Repository
	 * @throws XDMException in case of processing error
	 */
	void afterInsert(Document doc, SchemaRepository repo) throws XDMException;
	
	/**
	 * fires before document updated in XDM Schema
	 * 
	 * @param doc the XDM document
	 * @param repo the XDM Schema Repository
	 * @throws XDMException in case of processing error
	 */
	void beforeUpdate(Document doc, SchemaRepository repo) throws XDMException;
	
	/**
	 * fires after document updated in XDM Schema
	 * 
	 * @param doc the XDM document
	 * @param repo the XDM Schema Repository
	 * @throws XDMException in case of processing error
	 */
	void afterUpdate(Document doc, SchemaRepository repo) throws XDMException;

	/**
	 * fires before document deleted from XDM Schema
	 * 
	 * @param doc the XDM document
	 * @param repo the XDM Schema Repository
	 * @throws XDMException in case of processing error
	 */
	void beforeDelete(Document doc, SchemaRepository repo) throws XDMException;

	/**
	 * fires after document deleted from XDM Schema
	 * 
	 * @param doc the XDM document
	 * @param repo the XDM Schema Repository
	 * @throws XDMException in case of processing error
	 */
	void afterDelete(Document doc, SchemaRepository repo) throws XDMException;
	
}
