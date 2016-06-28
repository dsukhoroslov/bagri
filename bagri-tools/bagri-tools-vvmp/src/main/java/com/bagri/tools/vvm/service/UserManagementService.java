package com.bagri.tools.vvm.service;

import java.util.List;

import com.bagri.tools.vvm.model.User;

public interface UserManagementService {
    List<User> getUsers() throws ServiceException;
    boolean addUser(String user, String password) throws ServiceException;
    boolean deleteUser(String user) throws ServiceException;
    boolean activateUser(String user, boolean activate) throws ServiceException;
    boolean changePassword(String user, String password) throws ServiceException;
}
