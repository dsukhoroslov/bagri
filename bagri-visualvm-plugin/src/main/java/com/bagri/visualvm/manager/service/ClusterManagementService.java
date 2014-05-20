package com.bagri.visualvm.manager.service;

import com.bagri.visualvm.manager.model.Node;

import javax.management.ObjectName;
import java.util.List;

public interface ClusterManagementService {
    Node getNode(ObjectName name) throws ServiceException;
    List<Node> getNodes() throws ServiceException;
    void saveNode(Node node) throws ServiceException;
    void addNode(Node node) throws ServiceException;
    void deleteNode(Node node) throws ServiceException;
}
