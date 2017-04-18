package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
import static com.bagri.core.Constants.pn_node_instance;
//import static com.bagri.core.Constants.pn_log_level;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.xquery.XQItemType;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ModelManagement;

public class ConcurrentModelMgmntTest {
	
	@Test
	public void testPathRegistration() throws Exception {
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		//System.setProperty(pn_log_level, "trace");
		System.setProperty(pn_node_instance, "0");
		System.setProperty(pn_config_properties_file, "test.properties");
		System.setProperty(pn_config_path, "src\\test\\resources");
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	    SchemaRepositoryImpl repo = context.getBean(SchemaRepositoryImpl.class);
		ModelManagement mm = repo.getModelManagement();
		
		int thCount = 5;
		ExecutorService exec = Executors.newFixedThreadPool(thCount);
		List<Registrator> tasks = new ArrayList<>(thCount);
		for (int i=0; i < thCount; i++) {
			tasks.add(new Registrator(mm));
		}
		
		List<Integer> paths = new ArrayList<>(thCount);
		List<Future<Integer>> results = exec.invokeAll(tasks);
		for (Future<Integer> dt: results) {
			paths.add(dt.get());
		}

		Set<Integer> ids = mm.getPathElements("/{http://tpox-benchmark.com/security}Security");
		System.out.println("ids: " + ids);
		Path path = mm.getPath("/{http://tpox-benchmark.com/security}Security", "/{http://tpox-benchmark.com/security}Security");
		for (int pt: paths) {
			if (pt != path.getPathId()) {
				fail("got different paths: " + paths + ", when expected: " + path.getPathId());
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
			Path path = mm.translatePath("/{http://tpox-benchmark.com/security}Security", 
					"/{http://tpox-benchmark.com/security}Security", ///{http://tpox-benchmark.com/security}Symbol/text()", 
					NodeKind.element, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne);
					//NodeKind.text, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne);
			return path.getPathId();
		}
		
	}

}
