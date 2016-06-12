package com.bagri.xdm.domain;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMRepository;

/**
 * Represents trigger to run at various points of document life cycle 
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface XDMTrigger {

	/**
	 * fires before document inserted into XDM Schema
	 * 
	 * @param doc the XDM document
	 * @param repo the XDM Schema Repository
	 * @throws XDMException in case of processing error
	 */
	void beforeInsert(XDMDocument doc, XDMRepository repo) throws XDMException;
	
	/**
	 * fires after document inserted into XDM Schema
	 * 
	 * @param doc the XDM document
	 * @param repo the XDM Schema Repository
	 * @throws XDMException in case of processing error
	 */
	void afterInsert(XDMDocument doc, XDMRepository repo) throws XDMException;
	
	/**
	 * fires before document updated in XDM Schema
	 * 
	 * @param doc the XDM document
	 * @param repo the XDM Schema Repository
	 * @throws XDMException in case of processing error
	 */
	void beforeUpdate(XDMDocument doc, XDMRepository repo) throws XDMException;
	
	/**
	 * fires after document updated in XDM Schema
	 * 
	 * @param doc the XDM document
	 * @param repo the XDM Schema Repository
	 * @throws XDMException in case of processing error
	 */
	void afterUpdate(XDMDocument doc, XDMRepository repo) throws XDMException;

	/**
	 * fires before document deleted from XDM Schema
	 * 
	 * @param doc the XDM document
	 * @param repo the XDM Schema Repository
	 * @throws XDMException in case of processing error
	 */
	void beforeDelete(XDMDocument doc, XDMRepository repo) throws XDMException;

	/**
	 * fires after document deleted from XDM Schema
	 * 
	 * @param doc the XDM document
	 * @param repo the XDM Schema Repository
	 * @throws XDMException in case of processing error
	 */
	void afterDelete(XDMDocument doc, XDMRepository repo) throws XDMException;
	
}
