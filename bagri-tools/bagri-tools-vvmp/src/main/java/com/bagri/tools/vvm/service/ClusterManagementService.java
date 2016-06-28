package com.bagri.tools.vvm.service;

import javax.management.ObjectName;

import com.bagri.tools.vvm.model.Node;

import java.util.List;

public interface ClusterManagementService {
    Node getNode(ObjectName name) throws ServiceException;
    List<Node> getNodes() throws ServiceException;
    void saveNode(Node node) throws ServiceException;
    void addNode(Node node) throws ServiceException;
    void deleteNode(Node node) throws ServiceException;
}
