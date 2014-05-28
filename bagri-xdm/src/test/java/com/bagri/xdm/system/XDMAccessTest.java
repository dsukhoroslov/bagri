package com.bagri.xdm.system;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        
        assertTrue(access.getRoles().size() == 2);
        assertTrue(access.getUsers().size() == 2);
    }
	
	@Test
	public void testWrite() throws JAXBException {

		Map<String, XDMPermission> perms = new HashMap<String, XDMPermission>(2);
		perms.put("resource1", new XDMPermission("resource1", XDMPermission.Permission.read));
		perms.put("resource2", new XDMPermission("resource2", XDMPermission.Permission.read, XDMPermission.Permission.modify));
		XDMRole role = new XDMRole(1, new Date(), "test", perms, null, "TestRole", "Description");

		//perms.clear();
		//perms.add(new XDMPermission(XDMPermission.Permission.execute, "schema3"));
		XDMUser user = new XDMUser(1, new Date(), "test", null, null, "admin", "admin", true);
		user.addIncludedRole(role.getName());
		user.addPermission("schema3", XDMPermission.Permission.execute);

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
