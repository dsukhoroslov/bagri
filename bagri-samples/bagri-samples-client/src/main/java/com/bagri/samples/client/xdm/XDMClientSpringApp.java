package com.bagri.samples.client.xdm;

import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.XDMRepository;

public class XDMClientSpringApp extends XDMClientApp {

	public static void main(String[] args) throws Exception {
		
		ApplicationContext context = new ClassPathXmlApplicationContext("spring/xdm-client-context.xml");
		XDMRepository xRepo = context.getBean("xdmRepository", XDMRepository.class);
		
	    XDMClientSpringApp client = new XDMClientSpringApp(xRepo); 
		tester.testClient(client);
	}
	
	public XDMClientSpringApp(Properties props) {
		super(props);
	}
	
	public XDMClientSpringApp(XDMRepository xRepo) {
		super(xRepo);
	}	
	
}
