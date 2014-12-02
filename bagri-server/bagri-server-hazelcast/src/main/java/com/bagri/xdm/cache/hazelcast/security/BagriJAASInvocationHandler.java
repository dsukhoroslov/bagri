package com.bagri.xdm.cache.hazelcast.security;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXPrincipal;
import javax.management.remote.MBeanServerForwarder;
import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.security.LocalSubject;
import com.bagri.xdm.cache.hazelcast.management.UserManagement;
import com.bagri.xdm.cache.hazelcast.management.UserManager;
import com.bagri.xdm.system.XDMPermission;
import com.bagri.xdm.system.XDMPermission.Permission;

public class BagriJAASInvocationHandler implements InvocationHandler {

    private static final transient Logger logger = LoggerFactory.getLogger(BagriJAASInvocationHandler.class);
	
	private MBeanServer mbs;
	private UserManagement autzManager;
	
	private BagriJAASInvocationHandler(UserManagement autzManager) {
		this.autzManager = autzManager;
	}
	
	public static MBeanServerForwarder newProxyInstance(UserManagement autzManager) {

		Object proxy = Proxy.newProxyInstance(
				MBeanServerForwarder.class.getClassLoader(), 
				new Class[] {MBeanServerForwarder.class}, 
				new BagriJAASInvocationHandler(autzManager));

		return MBeanServerForwarder.class.cast(proxy);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		logger.trace("invoke.enter; method: {}; args: {}", method.getName(), args); 

		final String methodName = method.getName();

		if (methodName.equals("getMBeanServer")) {
			return mbs;
		}

		if (methodName.equals("setMBeanServer")) {
			if (args[0] == null) {
				throw new IllegalArgumentException("Null MBeanServer");
			}
			if (mbs != null) {
				throw new IllegalArgumentException("MBeanServer already initialized");
			}
			mbs = (MBeanServer) args[0];
			return null;
		}

		if (args == null || args.length == 0 || !(args[0] instanceof ObjectName)) {
			// some MBeanServer method
			return method.invoke(mbs, args);
		}
		
		// Retrieve Subject from current AccessControlContext
		AccessControlContext acc = AccessController.getContext();
		Subject subject = Subject.getSubject(acc);

		logger.trace("invoke; got Subject: {}", subject); 
		
		// Allow operations performed locally on behalf of the connector server
		// itself
		if (subject == null) {
			return method.invoke(mbs, args);
		} else {
			//
			LocalSubject.setSubject(subject);
		}

		// Restrict access to "createMBean" and "unregisterMBean" to any user
		//if (methodName.equals("createMBean") || methodName.equals("unregisterMBean")) {
		//	throw new SecurityException("Access denied");
		//}
		
		// Retrieve JMXPrincipal from Subject
		Set<JMXPrincipal> principals = subject.getPrincipals(JMXPrincipal.class);
		if (principals == null || principals.isEmpty()) {
			throw new SecurityException("No principal found. Access denied");
		}
		Principal principal = principals.iterator().next();
		String identity = principal.getName();
		ObjectName oName = (ObjectName) args[0];
		
		if (checkPermissions(identity, methodName, oName)) {
			return method.invoke(mbs, args);
		}

		throw new SecurityException("Access denied to resource: " + oName.toString());
	}
	
	public boolean checkPermissions(String identity, String methodName, ObjectName target) {

		logger.trace("checkPermissions.enter; identity: {}; method: {}; target: {}", identity, methodName, target); 
		
		if (!JMXUtils.domain.equals(target.getDomain())) {
			// grant access to other domains
			logger.trace("checkPermissions.exit; returning: true"); 
			return true;
		}
		
		UserManager uMgr = (UserManager) autzManager.getEntityManager(identity);
		if (uMgr == null) {
			// unknown user, shouldn't be this
			logger.trace("checkPermissions.exit; returning: false"); 
			return false;
		}
		
		//Map<String, XDMPermission> xPerms = uMgr.getAllPermissions();
		Map<String, XDMPermission> xPerms = uMgr.getFlatPermissions();
		XDMPermission xPerm = xPerms.get(target.toString());
		if (xPerm == null) {
			//no permissions granted to this resource
			logger.trace("checkPermissions.exit; returning: false"); 
			return false;
		}

		if ((methodName.startsWith("getAttribute") || methodName.equals("getMBeanInfo") 
				|| methodName.equals("isInstanceOf") || methodName.equals("isRegistered")
				|| methodName.equals("queryMBeans") || methodName.equals("queryNames"))
				&& xPerm.hasPermission(Permission.read)) {
			// granted read access
			logger.trace("checkPermissions.exit; returning: true"); 
			return true;
		}

		if (methodName.startsWith("setAttribute") && xPerm.hasPermission(Permission.modify)) {
			// granted write access
			logger.trace("checkPermissions.exit; returning: true"); 
			return true;
		}

		if (methodName.equals("invoke") && xPerm.hasPermission(Permission.execute)) {
			// granted execute access
			logger.trace("checkPermissions.exit; returning: true"); 
			return true;
		}

		logger.trace("checkPermissions.exit; returning: false"); 
		return false;
	}

}
