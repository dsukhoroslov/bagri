package com.bagri.tools.jmx;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

import com.bagri.tools.jmx.JMXInvoke;
import com.bagri.tools.jmx.JMXScript;

public class JMXScriptTest {
	
	@Test
	public void testWrite() throws JAXBException {
		
		JMXInvoke invoke = new JMXInvoke("com.bagri.xdm:type=Schema,name=default,kind=ModelManagement",
				"registerSchema", null, null);
		invoke.addArgument("java.lang.String", "../../etc/samples/tpox/security.xsd");

		JMXScript script = new JMXScript();
		script.getTasks().add(invoke);
		script.getTasks().add(5000);
		
		invoke = new JMXInvoke("com.bagri.xdm:type=Schema,name=default,kind=IndexManagement",
				"addIndex", null, null);
		invoke.addArgument("java.lang.String", "IDX_Customer_id");
		invoke.addArgument("java.lang.String", "/{http://tpox-benchmark.com/custacc}Customer/@id");
		invoke.addArgument("java.lang.String", "/{http://tpox-benchmark.com/custacc}Customer");
		invoke.addArgument("boolean", "true");
		invoke.addArgument("java.lang.String", "Customer id");
		script.getTasks().add(invoke);
		
		JAXBContext jc = JAXBContext.newInstance(JMXScript.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "file:///C:/Documents%20and%20Settings/mojalal/Desktop/FirstXSD.xml");
        marshaller.marshal(script, System.out);
	}
	
}
