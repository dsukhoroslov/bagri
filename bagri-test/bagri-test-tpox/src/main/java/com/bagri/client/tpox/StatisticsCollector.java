package com.bagri.client.tpox;

import java.io.IOException;
import java.net.MalformedURLException;
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
    private ObjectName mName;
	
	public static void main(String[] args) {

		String address = args[0];
		String schema = args[1];
		String file = args[2];
		String marker= args[3];
		String method = args[4];
		System.out.println("got address: " + address + "; schema: " + schema + "; method: " + method);

		try {
			StatisticsCollector sc = new StatisticsCollector(address, schema);
			String stats = sc.getStatistics(method);
			stats = marker + "; " + stats;
			System.out.println("got stats: " + stats + " will append to the file: " + file);
			stats += System.lineSeparator();
			FileUtils.appendTextFile(file, stats);
			sc.resetStatistics();
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
        mName = new ObjectName("com.bagri.xdm:type=Schema,name=" + schema + ",kind=DocumentManagement"); 
        HashMap environment = new HashMap();
        //environment.put(JMXConnector.CREDENTIALS, new String[] {"SDV", "TPoX"});
        environment.put(JMXConnector.CREDENTIALS, new String[] {"admin", "password"});

        jmxc = JMXConnectorFactory.connect(new JMXServiceURL(url), environment);
        mbsc = jmxc.getMBeanServerConnection();
	}
	
	public String getStatistics(String method) throws Exception {
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
		mbsc.invoke(mName, "resetStatistics", null, null);
		jmxc.close();
	}

}
