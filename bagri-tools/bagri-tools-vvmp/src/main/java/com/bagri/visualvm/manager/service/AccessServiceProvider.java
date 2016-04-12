package com.bagri.visualvm.manager.service;

import java.util.*;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.bagri.visualvm.manager.model.User;

public class AccessServiceProvider implements UserManagementService {
	
    private static final Logger LOGGER = Logger.getLogger(ClusterServiceProvider.class.getName());
    private final MBeanServerConnection connection;

    public AccessServiceProvider(MBeanServerConnection connection) {
        this.connection = connection;
    }

    @Override
    public List<User> getUsers() throws ServiceException {
        List<User> result;
        try {
            Object res = connection.invoke(new ObjectName("com.bagri.xdm:type=Management,name=UserManagement"), "getUserNames", null, null);
            String[] usersArray = (String[]) res;
            result = new ArrayList<User>(usersArray.length);
            for (String strUser : usersArray) {
                User u = new User(strUser);
                u.setActive(true);
                result.add(u);
            }
            return result;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getUserNames", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public boolean addUser(String user, String password) throws ServiceException {
        try {
            Object res = connection.invoke(new ObjectName("com.bagri.xdm:type=Management,name=UserManagement"), "addUser",new Object[] {user, password}, new String[] {String.class.getName(), String.class.getName()});
            return (Boolean) res;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "addUser", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public boolean deleteUser(String user) throws ServiceException {
        try {
            Object res = connection.invoke(new ObjectName("com.bagri.xdm:type=Management,name=UserManagement"), "deleteUser",new Object[] {user}, new String[] {String.class.getName()});
            return (Boolean) res;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "deleteUser", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public boolean activateUser(String user, boolean activate) throws ServiceException {
        try {
            Object res = connection.invoke(new ObjectName("com.bagri.xdm:type=Management,name=UserManagement"), "activateUser",new Object[] {user, activate}, new String[] {String.class.getName(), boolean.class.getName()});
            return (Boolean) res;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "activateUser", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public boolean changePassword(String user, String password) throws ServiceException {
        try {
            Object res = connection.invoke(new ObjectName("com.bagri.xdm:type=Management,name=UserManagement"), "changePassword",new Object[] {user, password}, new String[] {String.class.getName(), String.class.getName()});
            return (Boolean) res;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "changePassword", e);
            throw new ServiceException(e);
        }
    }


}
