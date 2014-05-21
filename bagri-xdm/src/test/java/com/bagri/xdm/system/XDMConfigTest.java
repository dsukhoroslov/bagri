package com.bagri.xdm.system;

import static org.junit.Assert.*;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

public class XDMConfigTest {

	@Test
	public void testRead() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XDMConfig.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        File xml = new File("src/test/resources/TPoXSchema.xml");
        XDMConfig config = (XDMConfig) unmarshaller.unmarshal(xml);
        assertNotNull(config);
        assertTrue(config.getNodes().size() == 0);
        assertTrue(config.getSchemas().size() == 2);
    }
	
	public void testWrite() {
		
        //Marshaller marshaller = jc.createMarshaller();
        //marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "file:///C:/Documents%20and%20Settings/mojalal/Desktop/FirstXSD.xml");
        //marshaller.marshal(config, System.out);
		
	}

}
