package com.bagri.xdm.cache.hazelcast;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Test;

public class XDMCacheServerTest {
	
	private static String url = "service:jmx:rmi://localhost/jndi/rmi://localhost:3333/jmxrmi";
	private static String user = "admin";
	private static String password = "admin";

	@Test
	public void testJMXConnection() throws Exception {

        HashMap env = new HashMap();
        String[] creds = new String[] {user, password};
        env.put(JMXConnector.CREDENTIALS, creds);

        JMXServiceURL jmxUrl = new JMXServiceURL(url);
        //JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl, env);
        
        JMXConnector jmxc = JMXConnectorFactory.newJMXConnector(jmxUrl, null);
        jmxc.connect(env);
        
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        String[] domains = mbsc.getDomains();
        assertTrue(containsDomain(domains, "com.bagri.xdm"));
        
        ObjectName name = new ObjectName("com.bagri.xdm:type=Management,name=ClusterManagement");
        Object nodes = mbsc.getAttribute(name, "Nodes");
        //System.out.println("got nodes: " + Arrays.toString((String[]) nodes));
        String[] sNodes = (String[]) nodes;
        assertTrue(sNodes.length > 0);
	}
	
	private boolean containsDomain(String[] domains, String domain) {
		for (String d : domains) {
			if (d.equals(domain)) return true;
		}
		return false;
	}

}
