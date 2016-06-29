package com.bagri.xdm.cache.hazelcast.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.util.XMLUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.cache.api.DocumentTrigger;
import com.bagri.xdm.domain.Document;
import com.bagri.xquery.api.XQProcessor;

public class XQueryTriggerImpl implements DocumentTrigger {
	
	private static final transient Logger logger = LoggerFactory.getLogger(XQueryTriggerImpl.class);
	
	private String query;
	
	public XQueryTriggerImpl(String query) {
		this.query = query;
	}

	@Override
	public void beforeInsert(Document doc, SchemaRepository repo) throws XDMException {
		runTrigger(doc, (RepositoryImpl) repo);
	} 

	@Override
	public void afterInsert(Document doc, SchemaRepository repo) throws XDMException {
		runTrigger(doc, (RepositoryImpl) repo);
	}

	@Override
	public void beforeUpdate(Document doc, SchemaRepository repo) throws XDMException {
		runTrigger(doc, (RepositoryImpl) repo);
	}

	@Override
	public void afterUpdate(Document doc, SchemaRepository repo) throws XDMException {
		runTrigger(doc, (RepositoryImpl) repo);
	}

	@Override
	public void beforeDelete(Document doc, SchemaRepository repo) throws XDMException {
		runTrigger(doc, (RepositoryImpl) repo);
	}

	@Override
	public void afterDelete(Document doc, SchemaRepository repo) throws XDMException {
		runTrigger(doc, (RepositoryImpl) repo);
	}
	
	private void runTrigger(Document doc, RepositoryImpl repo) throws XDMException {
		XQProcessor xqp = repo.getXQProcessor();
		QName var = new QName("doc");
		try {
			String xml = repo.getDocumentManagement().getDocumentAsString(doc.getUri());
			org.w3c.dom.Document xDoc = XMLUtils.textToDocument(xml);
			XQDataFactory xqFactory = xqp.getXQDataFactory();
			XQItem item = xqFactory.createItemFromNode(xDoc, xqFactory.createDocumentType());
			xqp.bindVariable(var, item);
			Properties props = new Properties();
			Iterator<?> iter = xqp.executeXQuery(query, props);
			if (logger.isTraceEnabled()) {
				while (iter.hasNext()) {
					logger.trace("runTrigger; result: {}", iter.next()); 
				}
			}
			xqp.unbindVariable(var);
		} catch (IOException ex) {
			throw new XDMException(ex, XDMException.ecInOut);
		} catch (XQException ex) {
			throw new XDMException(ex, XDMException.ecQuery);
		}
	}

}
