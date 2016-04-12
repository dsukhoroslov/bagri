package com.bagri.visualvm.manager.service;

import java.util.List;

import com.bagri.visualvm.manager.model.Collection;
import com.bagri.visualvm.manager.model.Document;

public interface DocumentManagementService {

    List<Collection> getCollections() throws ServiceException;
    void addCollection(Collection collection) throws ServiceException;
    Collection getCollection(String collection) throws ServiceException;
    void deleteCollection(String collection) throws ServiceException;
	
    List<Document> getDocuments(String collection) throws ServiceException;
    void addDocument(Document document) throws ServiceException;
    Document getDocument(String uri) throws ServiceException;
    String getDocumentContent(String uri) throws ServiceException;
    void deleteDocument(String uri) throws ServiceException;

}
