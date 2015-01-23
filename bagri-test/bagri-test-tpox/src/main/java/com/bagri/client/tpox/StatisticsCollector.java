package com.bagri.client.tpox;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.bagri.common.util.FileUtils;

public class StatisticsCollector {
	
	private String jmxAddress;
	private String schema;

    private MBeanServerConnection mbsc;
    private JMXConnector jmxc;
	
	public static void main(String[] args) {

		String address = args[0];
		String schema = args[1];
		String file = args[2];
		String marker= args[3];
		String method = args[4];
		String clear = args[5];
		System.out.println("got address: " + address + "; schema: " + schema + 
				"; method: " + method + "; clear: " + clear);

		try {
			StatisticsCollector sc = new StatisticsCollector(address, schema);

			String stats = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + "; " + marker;
			stats += "; " + sc.getActiveNodes();
			stats += System.lineSeparator();
			stats += sc.getStatistics(method);
			System.out.println("got stats: " + stats + " will append to the file: " + file);
			stats += System.lineSeparator();
			FileUtils.appendTextFile(file, stats);
			sc.resetStatistics();
			if ("true".equalsIgnoreCase(clear)) {
				sc.clearSchema(true);
			}
			sc.jmxc.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
	
	public StatisticsCollector(String jmxAddress, String schema) throws Exception {
		this.jmxAddress = jmxAddress;
		this.schema = schema;
        String url = "service:jmx:rmi:///jndi/rmi://" + jmxAddress + "/jmxrmi";
        HashMap environment = new HashMap();
        // TODO: get creds securely from outside..
        //environment.put(JMXConnector.CREDENTIALS, new String[] {"SDV", "TPoX"});
        environment.put(JMXConnector.CREDENTIALS, new String[] {"admin", "password"});

        jmxc = JMXConnectorFactory.connect(new JMXServiceURL(url), environment);
        mbsc = jmxc.getMBeanServerConnection();
	}
	
	public String getActiveNodes() throws Exception {
        ObjectName mName = new ObjectName("com.bagri.xdm:name=" + schema + ",type=Schema"); 
		String[] nodes = (String[]) mbsc.getAttribute(mName, "ActiveNodes");
		return Arrays.toString(nodes);
	}
	
	public String getStatistics(String method) throws Exception {
        ObjectName mName = new ObjectName("com.bagri.xdm:type=Schema,name=" + schema + ",kind=DocumentManagement"); 
		TabularData data = (TabularData) mbsc.getAttribute(mName, "InvocationStatistics");
		CompositeData stats = data.get(new String[] {method});
		StringBuffer buff = new StringBuffer();
		for (String key: stats.getCompositeType().keySet()) {
			buff.append(key).append("=").append(stats.get(key));
			buff.append("; ");
		}
		return buff.toString();
	}
	
	public void resetStatistics() throws Exception {
        ObjectName mName = new ObjectName("com.bagri.xdm:type=Schema,name=" + schema + ",kind=DocumentManagement"); 
		mbsc.invoke(mName, "resetStatistics", null, null);
		//jmxc.close();
	}
	
	public void clearSchema(boolean evictOnly) throws Exception {
        ObjectName mName = new ObjectName("com.bagri.xdm:type=Schema,name=" + schema + ",kind=DocumentManagement"); 
		mbsc.invoke(mName, "clear", new Object[] {evictOnly}, new String[] {boolean.class.getName()});
		//jmxc.close();
	}

}
