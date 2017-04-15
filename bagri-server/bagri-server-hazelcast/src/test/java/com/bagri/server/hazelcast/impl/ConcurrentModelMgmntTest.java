package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
//import static com.bagri.core.Constants.pn_log_level;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.server.api.ModelManagement;

public class ConcurrentModelMgmntTest {
	
	//@Test
	//public void testNamespace() throws Exception {
	//	String ns = ((SchemaRepository) xRepo).getModelManagement().translateNamespace("http://tpox-benchmark.com/security", "ns1");
	//	assertEquals("ns1", ns);
	//}
	
	@Test
	@Ignore
	public void testDocumentType() throws Exception {
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		//System.setProperty(pn_log_level, "trace");
		System.setProperty(pn_config_properties_file, "test.properties");
		System.setProperty(pn_config_path, "src\\test\\resources");
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	    SchemaRepositoryImpl repo = context.getBean(SchemaRepositoryImpl.class);
		ModelManagement mm = repo.getModelManagement();
		
		int thCount = 9;
		ExecutorService exec = Executors.newFixedThreadPool(thCount);
		List<Registrator> tasks = new ArrayList<>(thCount);
		for (int i=0; i < thCount; i++) {
			tasks.add(new Registrator(mm));
		}
		
		List<Integer> types = new ArrayList<>(thCount);
		List<Future<Integer>> results = exec.invokeAll(tasks);
		for (Future<Integer> dt: results) {
			types.add(dt.get());
		}

		int type = mm.getDocumentType("/{http://tpox-benchmark.com/security}Security");
		for (int tp: types) {
			if (tp != type) {
				fail("got different document types: " + types + ", when expected: " + type);
			}
		}
	}
	
	private static class Registrator implements Callable<Integer> {
		
		private ModelManagement mm;
		
		Registrator(ModelManagement mm) {
			this.mm = mm;
		}

		@Override
		public Integer call() throws Exception {
			return mm.translateDocumentType("/{http://tpox-benchmark.com/security}Security");
		}
		
	}

}
