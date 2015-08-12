package com.bagri.xdm.cache.hazelcast.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.bagri.common.util.XMLUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMTrigger;
import com.bagri.xquery.api.XQProcessor;

public class XQueryTriggerImpl implements XDMTrigger {
	
	private static final transient Logger logger = LoggerFactory.getLogger(XQueryTriggerImpl.class);
	
	private String query;
	
	public XQueryTriggerImpl(String query) {
		this.query = query;
	}

	@Override
	public void beforeInsert(XDMDocument doc, XDMRepository repo) throws XDMException {
		runTrigger(doc, (RepositoryImpl) repo);
	} 

	@Override
	public void afterInsert(XDMDocument doc, XDMRepository repo) throws XDMException {
		runTrigger(doc, (RepositoryImpl) repo);
	}

	@Override
	public void beforeUpdate(XDMDocument doc, XDMRepository repo) throws XDMException {
		runTrigger(doc, (RepositoryImpl) repo);
	}

	@Override
	public void afterUpdate(XDMDocument doc, XDMRepository repo) throws XDMException {
		runTrigger(doc, (RepositoryImpl) repo);
	}

	@Override
	public void beforeDelete(XDMDocument doc, XDMRepository repo) throws XDMException {
		runTrigger(doc, (RepositoryImpl) repo);
	}

	@Override
	public void afterDelete(XDMDocument doc, XDMRepository repo) throws XDMException {
		runTrigger(doc, (RepositoryImpl) repo);
	}
	
	private void runTrigger(XDMDocument doc, RepositoryImpl repo) throws XDMException {
		XQProcessor xqp = repo.getXQProcessor();
		QName var = new QName("doc");
		try {
			String xml = repo.getDocumentManagement().getDocumentAsString(doc.getDocumentKey());
			Document xDoc = XMLUtils.textToDocument(xml);
			XQDataFactory xqFactory = xqp.getXQDataFactory();
			XQItem item = xqFactory.createItemFromNode(xDoc, xqFactory.createDocumentType());
			xqp.bindVariable(var, item);
			Properties props = new Properties();
			Iterator iter = xqp.executeXQuery(query, props);
			if (logger.isTraceEnabled()) {
				while (iter.hasNext()) {
					logger.trace("runTrigger; result: {}", iter.next()); 
				}
			}
			xqp.unbindVariable(var);
		} catch (XQException | IOException ex) {
			logger.error("runTrigger.error", ex); 
			throw new XDMException(ex);
		}
	}

}
