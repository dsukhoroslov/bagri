package com.bagri.samples.client.xdm;

import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.SchemaRepository;

public class XDMClientSpringApp extends XDMClientApp {

	public static void main(String[] args) throws Exception {
		
		ApplicationContext context = new ClassPathXmlApplicationContext("spring/xdm-client-context.xml");
		SchemaRepository xRepo = context.getBean("xdmRepository", SchemaRepository.class);
		
	    XDMClientSpringApp client = new XDMClientSpringApp(xRepo); 
		tester.testClient(client);
	}
	
	public XDMClientSpringApp(Properties props) {
		super(props);
	}
	
	public XDMClientSpringApp(SchemaRepository xRepo) {
		super(xRepo);
	}	
	
}
