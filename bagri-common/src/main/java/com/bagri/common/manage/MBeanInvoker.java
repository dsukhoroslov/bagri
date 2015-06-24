package com.bagri.common.manage;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.util.ReflectUtils;

public class MBeanInvoker implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(MBeanInvoker.class);

    private MBeanServerConnection mbsc;
    private JMXConnector jmxc;
	private JMXScript script;
    
    public static void main(String[] args) {
		String address = args[0];
		String login = args[1];
		String password = args[2];
		String file = args[3];
		try (MBeanInvoker mbi = new MBeanInvoker(address, login, password, file)) {
			mbi.run();
		} catch (Exception ex) {
			logger.error("main.error", ex);
		}
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MBeanInvoker(String jmxAddress, String login, String password, String fileName) throws Exception {
		//this.jmxAddress = jmxAddress;
        String url = "service:jmx:rmi:///jndi/rmi://" + jmxAddress + "/jmxrmi";
		HashMap environment = new HashMap();
        environment.put(JMXConnector.CREDENTIALS, new String[] {login, password});
        jmxc = JMXConnectorFactory.connect(new JMXServiceURL(url), environment);
        mbsc = jmxc.getMBeanServerConnection();
		logger.debug("<init>", "connected to MBean server at: {}", jmxAddress);
		
		JAXBContext jc = JAXBContext.newInstance(JMXScript.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        File file = new File(fileName);
		script = (JMXScript) unmarshaller.unmarshal(file); 
	}

	@Override
	public void close() throws IOException {
		jmxc.close();
	}
	
	public void run() throws Exception {
		for (Object task: script.getTasks()) {
			if (task instanceof JMXInvoke) {
				JMXInvoke invoke = (JMXInvoke) task;
				Object[] args = new Object[invoke.getArguments().size()];
				String[] types = new String[args.length];
				int idx = 0;
				for (JMXArgument arg: invoke.getArguments()) {
					args[idx] = getValue(arg);
					types[idx] = arg.getType();
					idx++;
				}
				invoke(invoke.getMBean(), invoke.getMethod(), args, types);
			} else if (task instanceof JMXGetAttribute) {
				JMXGetAttribute get = (JMXGetAttribute) task;
				Object o = getAttribute(get.getMBean(), get.getAttribute());
				logger.debug("run; got attribute: {}", o);
			} else if (task instanceof JMXSetAttribute) {
				JMXSetAttribute set = (JMXSetAttribute) task;
				setAttribute(set.getMBean(), set.getAttribute(), set.getValue());
				//logger.debug("run; got attribute: {}", o);
			} else if (task instanceof Integer) {
				Thread.sleep((Integer) task);
			}
		}
	}
	
	private Object getValue(JMXArgument arg) throws Exception {
		Class<?> cls = ReflectUtils.type2Wrapper(arg.getType());
		Constructor<?> c = cls.getConstructor(String.class);
		return c.newInstance(arg.getValue());
	}
	
	public Object getAttribute(String mbName, String aName) throws Exception {
        ObjectName oName = new ObjectName(mbName); 
		return mbsc.getAttribute(oName, aName);
	}
	
	public Object invoke(String mbName, String mName, Object[] args, String[] types) throws Exception {
		logger.debug("invoke.enter; MBean: {}; method: {}; args: {}; types: {}", mbName, mName, args, types);
        ObjectName oName = new ObjectName(mbName); 
		return mbsc.invoke(oName, mName, args, types);
	}
	
	public void setAttribute(String mbName, String aName, Object value) throws Exception {
        ObjectName oName = new ObjectName(mbName);
        Attribute  attr = new Attribute(aName, value);
		mbsc.setAttribute(oName, attr);
	}

}