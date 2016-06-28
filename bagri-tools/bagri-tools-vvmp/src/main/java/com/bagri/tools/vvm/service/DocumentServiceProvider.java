package com.bagri.tools.vvm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;

import com.bagri.tools.vvm.model.Collection;
import com.bagri.tools.vvm.model.Document;
import com.bagri.tools.vvm.util.FileUtil;

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
        	ObjectName on = getDocMgrObjectName();
            Object res = connection.getAttribute(on, "Collections");
            if (res == null) {
            	return result;
            }
            TabularData clns = (TabularData) res;
        	Set<List> keys = (Set<List>) clns.keySet();
            res = connection.getAttribute(on, "CollectionStatistics");
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
            				stsData.get("Number of fragments"), (Long) stsData.get("Consumed size"), ((Double) stsData.get("Avg size (bytes)")).intValue(), 
            				((Double) stsData.get("Avg size (elmts)")).intValue());
                } else {                	
                	cln = new Collection((String) clnData.get("name"), (String) clnData.get("description"), (String) clnData.get("created at"), 
                			(String) clnData.get("created by"), (Integer) clnData.get("id"), (Integer) clnData.get("version"), (String) clnData.get("document type"),
                			(Boolean) clnData.get("enabled"), 0, 0, 0, 0, 0, 0);
                }
        		result.add(cln);
        	}
            return result;
        } catch (Exception ex) {
            LOGGER.throwing(this.getClass().getName(), "getCollections", ex);
            throw new ServiceException(ex);
        }
	}

	@Override
	public void addCollection(Collection collection) throws ServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection getCollection(String collection) throws ServiceException {
        try {
        	ObjectName on = getDocMgrObjectName();
			if ("All Documents".equals(collection)) {
	            Object res = connection.getAttribute(on, "TotalCounts");
        		CompositeData stsData = (CompositeData) res;
		        return new Collection("All Documents", "All Schema Documents", "", "", 0, 0, "", true, (Integer) stsData.get("Number of documents"), 
		        		(Integer) stsData.get("Number of elements"), (Integer) stsData.get("Number of fragments"), (Long) stsData.get("Consumed size"), 
		        		((Double) stsData.get("Avg size (bytes)")).intValue(), ((Double) stsData.get("Avg size (elmts)")).intValue());
			}
			
        	Object res = connection.getAttribute(on, "Collections");
            if (res != null) {
	            TabularData clns = (TabularData) res;
	        	Set<List> keys = (Set<List>) clns.keySet();
	            res = connection.getAttribute(on, "CollectionStatistics");
	        	TabularData stats = (TabularData) res;
	        	for (List key: keys) {
	        		Object[] index = key.toArray();
	        		if (collection.equals(index[0])) {
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
		            				stsData.get("Number of fragments"), (Long) stsData.get("Consumed size"), ((Double) stsData.get("Avg size (bytes)")).intValue(), 
		            				((Double) stsData.get("Avg size (elmts)")).intValue());
		                } else {                	
		                	cln = new Collection((String) clnData.get("name"), (String) clnData.get("description"), (String) clnData.get("created at"), 
		                			(String) clnData.get("created by"), (Integer) clnData.get("id"), (Integer) clnData.get("version"), (String) clnData.get("document type"),
		                			(Boolean) clnData.get("enabled"), 0, 0, 0, 0, 0, 0);
		                }
		                return cln;
	        		}
	        	}
            }
            return null;
        } catch (Exception ex) {
            LOGGER.throwing(this.getClass().getName(), "getCollections", ex);
            throw new ServiceException(ex);
        }
	}

	@Override
	public void deleteCollection(String collection) throws ServiceException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<Document> getDocuments(String collection) throws ServiceException {
        List<Document> result = new ArrayList<>();
        try {
        	if ("All Documents".equals(collection)) {
        		collection = null;
        	}
            Object res = connection.invoke(getDocMgrObjectName(), "getCollectionDocuments", 
            		new Object[] {collection, null}, new String[] {String.class.getName(), String.class.getName()});
            if (res != null) {
            	java.util.Collection<String> ids = (java.util.Collection<String>) res;
            	for (String uri: ids) {
            		result.add(new Document(uri));
            	}
        	}
            return result;
        } catch (Exception ex) {
            LOGGER.throwing(this.getClass().getName(), "getDocuments", ex);
            throw new ServiceException(ex);
        }
	}

	@Override
	public Document storeDocument(String uri, java.util.Collection<String> collections) throws ServiceException {
    	String props = collectionsToProperties(collections);
        try {
			String docUri = (String) connection.invoke(getDocMgrObjectName(), "registerDocument", 
					new Object[] {uri, props}, new String[] {String.class.getName(), String.class.getName()});
			//LOGGER.info("storeDocument; got docKey: " + docKey + " for uri " + FileUtil.getFileName(uri));
			return new Document(docUri);
		} catch (Exception ex) {
            LOGGER.throwing(this.getClass().getName(), "storeDocument", ex);
            throw new ServiceException(ex);
		}
	}

	@Override
	public boolean storeDocuments(String uri, java.util.Collection<String> collections) throws ServiceException {
		LOGGER.info("storeDocuments; got uri: " + uri);
    	String props = collectionsToProperties(collections);
        try {
			Integer cnt = (Integer) connection.invoke(getDocMgrObjectName(), "registerDocuments", 
					new Object[] {uri, props}, new String[] {String.class.getName(), String.class.getName()});
			LOGGER.info("storeDocuments; registered " + cnt + " documents");
			return cnt > 0;
		} catch (Exception ex) {
            LOGGER.throwing(this.getClass().getName(), "storeDocuments", ex);
            throw new ServiceException(ex);
		}
	}

	@Override
	public Map<String, Object> getDocumentInfo(String uri) throws ServiceException {
        try {
			CompositeData info = (CompositeData) connection.invoke(getDocMgrObjectName(), "getDocumentInfo", 
					new Object[] {uri}, new String[] {String.class.getName()});
	        Map<String, Object> result = new HashMap<String, Object>();
	        if (info != null) {
		        CompositeType type = info.getCompositeType();
		        for (String name : type.keySet()) {
		            result.put(name, info.get(name));
		        }
	        }
			info = (CompositeData) connection.invoke(getDocMgrObjectName(), "getDocumentLocation", 
					new Object[] {uri}, new String[] {String.class.getName()});
	        if (info != null) {
		        CompositeType type = info.getCompositeType();
		        for (String name : type.keySet()) {
		            result.put(name, info.get(name));
		        }
	        }			
	        return result;
		} catch (Exception ex) {
            LOGGER.throwing(this.getClass().getName(), "getDocumentInfo", ex);
            throw new ServiceException(ex);
		}
	}

	@Override
	public String getDocumentContent(String uri) throws ServiceException {
        try {
			return (String) connection.invoke(getDocMgrObjectName(), "getDocumentContent", new Object[] {uri}, new String[] {String.class.getName()});
		} catch (Exception ex) {
            LOGGER.throwing(this.getClass().getName(), "getDocumentContent", ex);
            throw new ServiceException(ex);
		}
	}

	@Override
	public void deleteDocument(String uri) throws ServiceException {
        try {
			connection.invoke(getDocMgrObjectName(), "removeDocument", new Object[] {uri}, new String[] {String.class.getName()});
		} catch (Exception ex) {
            LOGGER.throwing(this.getClass().getName(), "deleteDocument", ex);
            throw new ServiceException(ex);
		}
	}
	
	private ObjectName getDocMgrObjectName() throws MalformedObjectNameException {
		return new ObjectName("com.bagri.xdm:type=Schema,name=" + schema + ",kind=DocumentManagement");
	}
	
	private String collectionsToProperties(java.util.Collection<String> collections) {
		if (collections == null) {
			return null;
		}
		StringBuffer buff = new StringBuffer("xdm.document.collections=");
		for (String cln: collections) {
			buff.append(cln).append(" ");
		}
		buff.append(";");
		return buff.toString();
	}

}
