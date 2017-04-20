package com.bagri.core.server.api.df.json;

import static com.bagri.support.util.FileUtils.readTextFile;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.bagri.core.model.Path;
import com.bagri.core.server.api.ContentModelProcessor;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.df.json.JsonModelProcessor;
import com.bagri.core.server.api.impl.ModelManagementImpl;

public class JsonModelProcessorTest {

	private ModelManagement model;
	private ContentModelProcessor modelPro;
	
	@Before
	public void setUp() throws Exception {
		model = new ModelManagementImpl();
		modelPro = new JsonModelProcessor(model);
	}

	//@After
	//public void tearDown() throws Exception {
	//}

	@Test
	public void registerPersonModelTest() throws Exception {
		String schema = readTextFile("..\\etc\\samples\\json\\person");
		modelPro.registerModel(schema);
		//Collection<Path> sec = getSecurityPath();
		//assertNotNull(sec);
		//assertTrue(sec.size() > 0);
	}

	
}
