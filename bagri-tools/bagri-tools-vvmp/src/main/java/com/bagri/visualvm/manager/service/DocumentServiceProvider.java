package com.bagri.visualvm.manager.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import com.bagri.visualvm.manager.model.Collection;
import com.bagri.visualvm.manager.model.Document;

public class DocumentServiceProvider implements DocumentManagementService {
	
	private static final Logger LOGGER = Logger.getLogger(DocumentServiceProvider.class.getName());
    private final MBeanServerConnection connection;
	private final String schema;

	public DocumentServiceProvider(MBeanServerConnection connection, String schemaName) {
        this.connection = connection;
		this.schema = schemaName;
	}

	@Override
	public List<Collection> getCollections() throws ServiceException {
        List<Collection> result = new ArrayList<>();
        try {
            Object res = connection.getAttribute(new ObjectName("com.bagri.xdm:type=Schema,name=" + schema + ",kind=DocumentManagement"), "Collections");
            if (res == null) {
            	return result;
            }
            TabularData clns = (TabularData) res;
        	Set<List> keys = (Set<List>) clns.keySet();
            res = connection.getAttribute(new ObjectName("com.bagri.xdm:type=Schema,name=" + schema + ",kind=DocumentManagement"), "CollectionStatistics");
        	TabularData stats = (TabularData) res;
        	for (List key: keys) {
        		Object[] index = key.toArray();
           		CompositeData clnData = clns.get(index);
        		CompositeData stsData = null;
                if (stats != null) {
            		stsData = stats.get(index);
                }
        		Collection cln;
                if (stsData != null) {
            		cln = new Collection((String) clnData.get("name"), (String) clnData.get("description"), (String) clnData.get("created at"), 
            				(String) clnData.get("created by"), (Integer) clnData.get("id"), (Integer) clnData.get("version"), (String) clnData.get("document type"),
            				(Boolean) clnData.get("enabled"), (Integer) stsData.get("Number of documents"), (Integer) stsData.get("Number of elements"), (Integer) 
            				stsData.get("Number of fragments"), (Long) stsData.get("Consumed size"), (Double) stsData.get("Avg size (bytes)"), (Double) 
            				stsData.get("Avg size (elmts)"));
                } else {                	
                	cln = new Collection((String) clnData.get("name"), (String) clnData.get("description"), (String) clnData.get("created at"), 
                			(String) clnData.get("created by"), (Integer) clnData.get("id"), (Integer) clnData.get("version"), (String) clnData.get("document type"),
                			(Boolean) clnData.get("enabled"), 0, 0, 0, 0, 0.0, 0.0);
                }
        		result.add(cln);
        	}
            return result;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getCollections", e);
            throw new ServiceException(e);
        }
	}

	@Override
	public void addCollection(Collection collection) throws ServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection getCollection(String collection) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteCollection(String collection) throws ServiceException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<Document> getDocuments(String collection) throws ServiceException {
        List<Document> result = new ArrayList<>();
        try {
            Object res = connection.invoke(new ObjectName("com.bagri.xdm:type=Schema,name=" + schema + ",kind=DocumentManagement"), 
            		"getCollectionDocuments", new Object[] {collection, null}, new String[] {String.class.getName(), String.class.getName()});
    		LOGGER.info("got ids: " + res + " for collection " + collection);
            if (res != null) {
            	TabularData ids = (TabularData) res;
            	Set<List> keys = (Set<List>) ids.keySet();
            	for (List key: keys) {
            		Object[] index = key.toArray();
            		CompositeData idData = ids.get(index);
            		LOGGER.info("idData: " + idData);
            		result.add(new Document((Long) idData.get("docKey"), (String) idData.get("uri")));
            	}
        	}
            return result;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getDocuments", e);
            throw new ServiceException(e);
        }
	}

	@Override
	public void addDocument(Document document) throws ServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Document getDocument(String uri) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentContent(String uri) throws ServiceException {
        try {
			return (String) connection.invoke(getDocMgrObjectName(schema), "getDocumentContent", new Object[] {uri}, new String[] {String.class.getName()});
		} catch (Exception ex) {
            LOGGER.throwing(this.getClass().getName(), "getDocumentContent", ex);
            throw new ServiceException(ex);
		}
	}

	@Override
	public void deleteDocument(String uri) throws ServiceException {
		// TODO Auto-generated method stub	
	}
	
	private ObjectName getDocMgrObjectName(String schemaName) throws MalformedObjectNameException {
		return new ObjectName("com.bagri.xdm:type=Schema,name=" + schemaName + ",kind=DocumentManagement");
	}

}
