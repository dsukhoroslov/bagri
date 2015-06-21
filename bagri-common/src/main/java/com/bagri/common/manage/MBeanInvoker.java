package com.bagri.common.manage;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBeanInvoker implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(MBeanInvoker.class);
	//private String jmxAddress;
    private MBeanServerConnection mbsc;
    private JMXConnector jmxc;
    
    public static void main(String[] args) {
		String address = args[0];
		String login = args[1];
		String password = args[2];
		try (MBeanInvoker mbi = new MBeanInvoker(address, login, password)) {
			String mbean = args[3];
			int size = args.length - 4;
			if (size == 1) {
				// get attribute.. what for??
				String attr = args[4];
				mbi.getAttribute(mbean, attr);
			} else if (size > 1) {
				String method = args[4];
				size = size/2;
				String[] params = new String[size];
				String[] types = new String[size];
				for (int i=0; i < size; i++) {
					params[i] = args[i*2 + 5];
					types[i] = args[i*2 + 6];
				}
				mbi.invoke(mbean, method, params, types);
			} else {
				// throw ex..
			}
		} catch (Exception ex) {
			logger.error("main.error", ex);
		}
    }
	
	public MBeanInvoker(String jmxAddress, String login, String password) throws Exception {
		//this.jmxAddress = jmxAddress;
        String url = "service:jmx:rmi:///jndi/rmi://" + jmxAddress + "/jmxrmi";
        HashMap environment = new HashMap();
        environment.put(JMXConnector.CREDENTIALS, new String[] {login, password});
        jmxc = JMXConnectorFactory.connect(new JMXServiceURL(url), environment);
        mbsc = jmxc.getMBeanServerConnection();
		logger.debug("<init>", "connected to MBean server at: {}", jmxAddress);
	}

	@Override
	public void close() throws IOException {
		jmxc.close();
	}
	
	public Object getAttribute(String mbName, String aName) throws Exception {
        ObjectName oName = new ObjectName(mbName); 
		return mbsc.getAttribute(oName, aName);
	}
	
	public Object invoke(String mbName, String mName, Object[] args, String[] types) throws Exception {
		logger.trace("invoke.enter; MBean: {}; method: {}; args: {}; types: {}", mbName, mName, args, types);
        ObjectName oName = new ObjectName(mbName); 
		return mbsc.invoke(oName, mName, args, types);
	}

}