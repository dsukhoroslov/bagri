package com.bagri.xdm.client.hazelcast.impl;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMBindingManagement;

public class BindingManagementImpl implements XDMBindingManagement {
	
    private final static Logger logger = LoggerFactory.getLogger(BindingManagementImpl.class);
	
	private RepositoryImpl repo;
	
	void initialize(RepositoryImpl repo) {
		this.repo = repo;
	}	

	@Override
	public <T> T getDocumentBinding(long docId) {
		
		return null;
	}

	@Override
	public <T> T getDocumentBinding(long docId, T type) {
		
        return null;
	}

	@Override
	public Object getDocumentBinding(long docId, Class type) {

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
        return result;
	}
	
}
