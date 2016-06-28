package com.bagri.samples.client.xqj;

import java.util.Properties;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class XQJClientSpringApp extends XQJClientApp {

	public static void main(String[] args) throws Exception {
		
		ApplicationContext context = new ClassPathXmlApplicationContext("spring/xqj-client-context.xml");
		XQConnection xqc = context.getBean("xqConnection", XQConnection.class);
		
	    XQJClientSpringApp client = new XQJClientSpringApp(xqc); 
		tester.testClient(client);
	}
	
	public XQJClientSpringApp(Properties props) throws XQException {
		super(props);
	}
	
	public XQJClientSpringApp(XQConnection xqConn) {
		super(xqConn);
	}	

}
