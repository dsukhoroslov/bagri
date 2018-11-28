package com.bagri.core.system;

import static com.bagri.core.Constants.xs_ns;
import static com.bagri.core.Constants.xs_prefix;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

import org.junit.Test;

import com.bagri.core.system.Collection;
import com.bagri.core.system.Config;
import com.bagri.core.system.Index;
import com.bagri.core.system.JavaTrigger;
import com.bagri.core.system.Node;
import com.bagri.core.system.Schema;
import com.bagri.core.system.TriggerDefinition;
import com.bagri.core.system.XQueryTrigger;
import com.bagri.core.system.TriggerAction.Order;
import com.bagri.core.system.TriggerAction.Scope;

public class ConfigTest {

	@Test
	public void testRead() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(Config.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        File xml = new File("src/test/resources/test_config.xml");
        Config config = (Config) unmarshaller.unmarshal(xml);
        assertNotNull(config);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(config, System.out);
        
        assertTrue(config.getNodes().size() == 0);
        assertTrue(config.getSchemas().size() == 2);
        assertTrue(config.getModules().size() == 0);
        Schema test = config.getSchemas().get(1);
        assertTrue(test.getIndexes().size() == 2);
        Index index = test.getIndex("idx_test"); 
        assertNotNull(index);
        assertNotNull(index.getDataType());
        assertEquals("string", index.getDataType().getLocalPart());
        index = test.getIndex("IDX_Security_Yield"); 
        assertNotNull(index);
        assertNotNull(index.getDataType());
        assertEquals("float", index.getDataType().getLocalPart());
    }
	
	@Test
	public void testWrite() throws JAXBException {
		
		Properties props = new Properties();
		props.setProperty("bdb.schema.password", "test");
		Schema schema = new Schema(1, new Date(), "test", "Test", "description", false, props);
		Collection collection = new Collection(1, new Date(), "test", 1, "cln_security",  
				"/{http://tpox-benchmark.com/security}Security", null, "description", true);
		schema.addCollection(collection);
		Index index = new Index(1, new Date(), "test", "idx_test",  
				"/{http://tpox-benchmark.com/security}Security", "/Security", "/Security/Symbol",
				new QName(xs_ns, "string", xs_prefix), true, false, true, "description", true);
		schema.addIndex(index);
		TriggerDefinition javaTrigger = new JavaTrigger(1, new Date(), "test", "sample_library", "my.class.Name", true, true, "cln_security");
		javaTrigger.addAction(0, Order.before, Scope.commit);
		javaTrigger.addAction(1, Order.after, Scope.update);
		schema.addTrigger(javaTrigger);
		TriggerDefinition xqTrigger = new XQueryTrigger(1, new Date(), "test", "sample_module",	"trg:function", true, true, "cln_security");
		xqTrigger.addAction(0, Order.before, Scope.commit);
		schema.addTrigger(xqTrigger);
		Resource res = new Resource(1, new Date(), "test", "sample_resource", "/test", "description", "test_module", true);
		schema.addResource(res);
		MaterializedView view = new MaterializedView(1, new Date(), "test", "sample_view", 1, "test query", "description", true);
		schema.addView(view);
		
		Config config = new Config();
		config.getSchemas().add(schema);
		
		Node node = new Node(1, new Date(), "test", "first", props);
		config.getNodes().add(node);

		JAXBContext jc = JAXBContext.newInstance(Config.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "file:///C:/Documents%20and%20Settings/mojalal/Desktop/FirstXSD.xml");
        marshaller.marshal(config, System.out);
	}

	//@Test
	//public void testSchema() throws JAXBException, IOException {
	//	
	//	JAXBContext jc = JAXBContext.newInstance(XDMConfig.class);
	//	jc.generateSchema(new TestSchemaOutputResolver());
	//}
	
	
	class TestSchemaOutputResolver extends SchemaOutputResolver {
	    public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
	        //return new StreamResult(new File(baseDir,suggestedFileName));
	    	return null;
	    }
	}	
}
