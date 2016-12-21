package com.bagri.core.system;

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

import com.bagri.core.system.Access;
import com.bagri.core.system.Permission;
import com.bagri.core.system.Role;
import com.bagri.core.system.User;

public class AccessTest {

	@Test
	public void testRead() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(Access.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        File xml = new File("src/test/resources/test_access.xml");
        Access access = (Access) unmarshaller.unmarshal(xml);
        assertNotNull(access);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(access, System.out);
        
        assertTrue(access.getRoles().size() == 2);
        assertTrue(access.getUsers().size() == 2);
    }
	
	@Test
	public void testWrite() throws JAXBException {

		Map<String, Permission> perms = new HashMap<String, Permission>(2);
		perms.put("resource1", new Permission("resource1", Permission.Value.read));
		perms.put("resource2", new Permission("resource2", Permission.Value.read, Permission.Value.modify));
		Role role = new Role(1, new Date(), "test", perms, null, "TestRole", "Description");

		//perms.clear();
		//perms.add(new XDMPermission(XDMPermission.Permission.execute, "schema3"));
		User user = new User(1, new Date(), "test", null, null, "admin", "admin", true);
		user.addIncludedRole(role.getName());
		user.addPermission("schema3", Permission.Value.execute);

		Access access = new Access();
		access.getRoles().add(role);
		access.getUsers().add(user);

		JAXBContext jc = JAXBContext.newInstance(Access.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "file:///C:/Documents%20and%20Settings/mojalal/Desktop/FirstXSD.xml");
        marshaller.marshal(access, System.out);
	}


}
