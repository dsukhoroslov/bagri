package com.bagri.visualvm.manager.service;

import java.util.List;
import java.util.Map;

import com.bagri.visualvm.manager.model.Collection;
import com.bagri.visualvm.manager.model.Document;

public interface DocumentManagementService {

    List<Collection> getCollections() throws ServiceException;
    void addCollection(Collection collection) throws ServiceException;
    Collection getCollection(String collection) throws ServiceException;
    void deleteCollection(String collection) throws ServiceException;
	
    List<Document> getDocuments(String collection) throws ServiceException;
    Document storeDocument(String uri) throws ServiceException;
    boolean storeDocuments(String uri) throws ServiceException;
    Map<String, Object> getDocumentInfo(String uri) throws ServiceException;
    String getDocumentContent(String uri) throws ServiceException;
    void deleteDocument(String uri) throws ServiceException;

}
