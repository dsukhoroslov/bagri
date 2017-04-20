package com.bagri.core.server.api.df.xml;

import static com.bagri.support.util.FileUtils.readTextFile;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.bagri.core.model.Path;
import com.bagri.core.server.api.ContentModelProcessor;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ModelManagementImpl;

public class XmlModelProcessorTest {

	private ModelManagement model;
	private ContentModelProcessor modelPro;
	
	@Before
	public void setUp() throws Exception {
		model = new ModelManagementImpl();
		modelPro = new XmlModelProcessor(model);
	}

	//@After
	//public void tearDown() throws Exception {
	//}

	public Collection<Path> getSecurityPath() {
		return model.getTypePaths("/{http://tpox-benchmark.com/security}Security");
	}
	
	public Collection<Path> getCustomerPath() {
		return model.getTypePaths("/{http://tpox-benchmark.com/custacc}Customer");
	}

	
	@Test
	public void registerSecurityPathTest() throws Exception {
		String schema = readTextFile("..\\etc\\samples\\tpox\\security.xsd");
		modelPro.registerModel(schema);
		Collection<Path> sec = getSecurityPath();
		assertNotNull(sec);
		assertTrue(sec.size() > 0);
	}

	@Test
	public void registerCustomerPathTest() throws Exception {
		String schema = readTextFile("..\\etc\\samples\\tpox\\custacc.xsd");
		modelPro.registerModel(schema);
		Collection<Path> sec = getCustomerPath();
		assertNotNull(sec);
		assertTrue(sec.size() > 0);
	}

}
