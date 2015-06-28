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
import com.bagri.xdm.api.XDMRepository;

public class BindingManagementImpl implements XDMBindingManagement {
	
    private final static Logger logger = LoggerFactory.getLogger(BindingManagementImpl.class);
	
	protected XDMRepository repo;
	
	void initialize(XDMRepository repo) {
		this.repo = repo;
	}	

	@Override
	public <T> T getDocumentBinding(long docId) {
		
		return null;
	}

	@Override
	public <T> T getDocumentBinding(long docId, Class<T> type) {
		
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
	        	logger.error("getDocumentBinding.error", ex);
	        }
		}
		logger.trace("getDocumentBinding.exit; returning: {}", result);
        return type.cast(result);
	}

	@Override
	public void setDocumentBinding(Object value) {
		
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
        	repo.getDocumentManagement().storeDocumentFromString(0, null, xml);
        } catch (JAXBException | IOException ex) {
        	logger.error("setDocumentBinding.error", ex);
        }
		logger.trace("setDocumentBinding.exit;");
	}
	
}
