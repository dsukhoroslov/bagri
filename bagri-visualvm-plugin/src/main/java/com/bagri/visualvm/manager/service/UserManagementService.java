package com.bagri.visualvm.manager.service;

import com.bagri.visualvm.manager.model.User;

import java.util.List;

public interface UserManagementService {
    List<User> getUsers() throws ServiceException;
    boolean addUser(String user, String password) throws ServiceException;
    boolean deleteUser(String user) throws ServiceException;
    boolean activateUser(String user, boolean activate) throws ServiceException;
    boolean changePassword(String user, String password) throws ServiceException;
}
