package com.bagri.tools.vvm.service;

import java.util.List;
import java.util.Map;

import com.bagri.tools.vvm.model.Collection;
import com.bagri.tools.vvm.model.Document;

public interface DocumentManagementService {

    List<Collection> getCollections() throws ServiceException;
    void addCollection(Collection collection) throws ServiceException;
    Collection getCollection(String collection) throws ServiceException;
    void deleteCollection(String collection) throws ServiceException;
	
    List<Document> getDocuments(String collection) throws ServiceException;
    Document storeDocument(String uri, java.util.Collection<String> collections) throws ServiceException;
    boolean storeDocuments(String uri, java.util.Collection<String> collections) throws ServiceException;
    Map<String, Object> getDocumentInfo(String uri) throws ServiceException;
    String getDocumentContent(String uri) throws ServiceException;
    void deleteDocument(String uri) throws ServiceException;

}
