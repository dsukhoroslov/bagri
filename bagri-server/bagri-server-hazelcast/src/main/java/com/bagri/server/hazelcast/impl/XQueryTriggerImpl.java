package com.bagri.server.hazelcast.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.DocumentTrigger;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.support.util.XMLUtils;

public class XQueryTriggerImpl implements DocumentTrigger {
	
	private static final transient Logger logger = LoggerFactory.getLogger(XQueryTriggerImpl.class);
	
	private String query;
	
	public XQueryTriggerImpl(String query) {
		this.query = query;
	}

	@Override
	public void beforeInsert(Document doc, SchemaRepository repo) throws BagriException {
		runTrigger(doc, (SchemaRepositoryImpl) repo);
	} 

	@Override
	public void afterInsert(Document doc, SchemaRepository repo) throws BagriException {
		runTrigger(doc, (SchemaRepositoryImpl) repo);
	}

	@Override
	public void beforeUpdate(Document doc, SchemaRepository repo) throws BagriException {
		runTrigger(doc, (SchemaRepositoryImpl) repo);
	}

	@Override
	public void afterUpdate(Document doc, SchemaRepository repo) throws BagriException {
		runTrigger(doc, (SchemaRepositoryImpl) repo);
	}

	@Override
	public void beforeDelete(Document doc, SchemaRepository repo) throws BagriException {
		runTrigger(doc, (SchemaRepositoryImpl) repo);
	}

	@Override
	public void afterDelete(Document doc, SchemaRepository repo) throws BagriException {
		runTrigger(doc, (SchemaRepositoryImpl) repo);
	}
	
	private void runTrigger(Document doc, SchemaRepositoryImpl repo) throws BagriException {
		XQProcessor xqp = repo.getXQProcessor();
		try {
			String xml = repo.getDocumentManagement().getDocumentAsString(doc.getUri(), null);
			org.w3c.dom.Document xDoc = XMLUtils.textToDocument(xml);
			XQDataFactory xqFactory = xqp.getXQDataFactory();
			XQItem item = xqFactory.createItemFromNode(xDoc, xqFactory.createDocumentType());
			xqp.bindVariable("doc", item);
			Properties props = new Properties();
			Iterator<?> iter = xqp.executeXQuery(query, props);
			if (logger.isTraceEnabled()) {
				while (iter.hasNext()) {
					logger.trace("runTrigger; result: {}", iter.next()); 
				}
			}
			xqp.unbindVariable("doc");
		} catch (IOException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		} catch (XQException ex) {
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}

}
