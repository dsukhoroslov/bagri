package com.bagri.xdm.process.hazelcast.schema;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_XDMPopulateSchemaTask;

import java.util.Properties;
import java.util.Set;

import org.springframework.context.ApplicationContext;

import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.access.hazelcast.impl.HazelcastSchemaDictionary;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMElement;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapLoaderLifecycleSupport;

public class SchemaPopulator extends SchemaDenitiator {
	
	public SchemaPopulator() {
		super();
	}
	
	public SchemaPopulator(String schemaName) {
		super(schemaName);
	}

	@Override
	public Boolean call() throws Exception {
    	logger.trace("call.enter; schema: {}", schemaName);
    	boolean result = false;
		// get hzInstance and close it...
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(schemaName);
		if (hz != null) {
			ApplicationContext ctx = (ApplicationContext) hz.getUserContext().get("appContext");
			populateSchema(hz, ctx);
			result = true;
		}
    	logger.trace("call.exit; schema {} populated: {}", schemaName, result);
		return result;
	}

	private void populateSchema(HazelcastInstance hz, ApplicationContext ctx) {
		//IMap dtCache = hz.getMap("dict-document-type");
		//MapConfig dtConfig = hz.getConfig().getMapConfig("dict-document-type");
		//MapStoreFactory msFactory = (MapStoreFactory) dtConfig.getMapStoreConfig().getFactoryImplementation();
		//MapLoader populator = msFactory.newMapStore("dict-document-type", properties);

		XDMSchemaDictionary schemaDict = ctx.getBean("xdmDictionary", HazelcastSchemaDictionary.class);
		//<bean id="eltCacheStore" class="com.bagri.xdm.cache.hazelcast.store.xml.ElementCacheStore">
		MapLoaderLifecycleSupport eltCacheStore = ctx.getBean("eltCacheStore", MapLoaderLifecycleSupport.class);
		
		String cacheName = "xdm-elements";
		Properties props = new Properties();
		props.put("xdmDictionary", schemaDict);
		eltCacheStore.init(hz, props, cacheName);
		
		IMap<XDMDataKey, XDMElement> xdmCache = hz.getMap(cacheName);
		Set<XDMDataKey> keys = xdmCache.localKeySet();
		xdmCache.getAll(keys);
    	logger.trace("populateSchema.exit; keys loadeds: {}", keys.size());
	}

	@Override
	public int getId() {
		return cli_XDMPopulateSchemaTask;
	}

}
