package com.bagri.xdm.system;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

public class XDMAccessTest {

	@Test
	public void testRead() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(XDMAccess.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        File xml = new File("src/test/resources/test_access.xml");
        XDMAccess access = (XDMAccess) unmarshaller.unmarshal(xml);
        assertNotNull(access);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(access, System.out);
        
        assertTrue(access.getRoles().size() == 1);
        assertTrue(access.getUsers().size() == 2);
    }
	
	@Test
	public void testWrite() throws JAXBException {

		XDMRole role = new XDMRole("TestRole", "Description", 1, new Date(), "test");
		role.getPermissions().put("resource1", XDMPermission.readonly);
		role.getPermissions().put("resource2", XDMPermission.readwrite);
		
		XDMUser user = new XDMUser("admin", "admin", true, 1, new Date(), "test");
		user.getAssignedRoles().add(role);
		user.getGrants().put("schema3", XDMPermission.execute);

		XDMAccess access = new XDMAccess();
		access.getRoles().add(role);
		access.getUsers().add(user);

		JAXBContext jc = JAXBContext.newInstance(XDMAccess.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "file:///C:/Documents%20and%20Settings/mojalal/Desktop/FirstXSD.xml");
        marshaller.marshal(access, System.out);
	}


}
