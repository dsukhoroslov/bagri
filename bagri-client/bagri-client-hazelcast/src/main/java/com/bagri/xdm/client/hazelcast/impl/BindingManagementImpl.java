package com.bagri.xdm.client.hazelcast.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMBindingManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.common.XDMDocumentId;

public class BindingManagementImpl implements XDMBindingManagement {
	
    private final static Logger logger = LoggerFactory.getLogger(BindingManagementImpl.class);
	
	protected XDMRepository repo;
	
	void initialize(XDMRepository repo) {
		this.repo = repo;
	}	

	@Override
	public <T> T getDocumentBinding(XDMDocumentId docId) throws XDMException {
		
		return null;
	}

	@Override
	public <T> T getDocumentBinding(XDMDocumentId docId, Class<T> type) throws XDMException {
		
		logger.trace("getDocumentBinding.enter; docId: {}; type: {}", docId, type);
		Object result = null;
		String xml = repo.getDocumentManagement().getDocumentAsString(docId);
		if (xml != null) {
	        try {
	        	// TODO think about internal static context
	        	JAXBContext jc = JAXBContext.newInstance(type);
	        	Unmarshaller unmarshaller = jc.createUnmarshaller();
	        	result = unmarshaller.unmarshal(new StringReader(xml));
	        } catch (JAXBException ex) {
	        	throw new XDMException(ex, XDMException.ecBinding);
	        }
		}
		logger.trace("getDocumentBinding.exit; returning: {}", result);
        return type.cast(result);
	}

	@Override
	public void setDocumentBinding(Object value) throws XDMException {
		
		logger.trace("setDocumentBinding.enter; value: {}", value);
        try {
        	// TODO: think about internal static context
        	JAXBContext jc = JAXBContext.newInstance(value.getClass());
        	Marshaller marshaller = jc.createMarshaller();
        	StringWriter writer = new StringWriter();
        	marshaller.marshal(value, writer);
        	writer.flush();
        	String xml = writer.getBuffer().toString();
        	writer.close();
        	// TODO: get docId/uri from value somehow?
        	repo.getDocumentManagement().storeDocumentFromString(null, xml, null);
        } catch (JAXBException ex) {
        	throw new XDMException(ex, XDMException.ecBinding);
        } catch (IOException ex) {
        	throw new XDMException(ex, XDMException.ecInOut);
        }
		logger.trace("setDocumentBinding.exit;");
	}
	
}
