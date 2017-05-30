package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.ctx_repo;
import static com.bagri.core.Constants.ctx_popService;
import static com.bagri.core.Constants.pn_schema_format_default;
import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.server.hazelcast.util.HazelcastUtils.hasStorageMembers;
import static com.bagri.server.hazelcast.util.HazelcastUtils.findSystemInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.bagri.core.KeyFactory;
import com.bagri.core.api.BindingManagement;
import com.bagri.core.api.TransactionManagement;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.impl.SchemaRepositoryBase;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ClientManagement;
import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ContentHandler;
import com.bagri.core.server.api.ContentModeler;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.IndexManagement;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.PopulationManagement;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.server.api.TriggerManagement;
import com.bagri.core.server.api.df.map.MapHandler;
import com.bagri.core.server.api.df.json.JsonpHandler;
import com.bagri.core.server.api.df.xml.XmlHandler;
import com.bagri.core.system.DataFormat;
import com.bagri.core.system.Index;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.system.TriggerDefinition;
import com.bagri.core.xquery.api.XQProcessor;
import com.hazelcast.core.HazelcastInstance;

public class SchemaRepositoryImpl extends SchemaRepositoryBase implements ApplicationContextAware, SchemaRepository {

	private static final transient Logger logger = LoggerFactory.getLogger(SchemaRepositoryImpl.class);
	
	private ThreadLocal<String> thClient = new ThreadLocal<String>() {
		
		@Override
		protected String initialValue() {
			return null;
 		}
	};
	
	private KeyFactory xdmFactory; 
	//private String instanceNum;
	private Schema xdmSchema;
	private Map<String, DataFormat> xdmFormats;
	private Collection<Module> xdmModules;
	private Collection<Library> xdmLibraries;
    private ClientManagement clientMgr;
    private IndexManagement indexMgr;
	private ModelManagement modelMgr;
	private PopulationManagement popMgr;
    private TriggerManagement triggerMgr;
    private ApplicationContext appContext;
    private HazelcastInstance hzInstance;
	private Map<String, XQProcessor> processors = new ConcurrentHashMap<String, XQProcessor>();

	private ConcurrentHashMap<String, ContentHandler> handlers = new ConcurrentHashMap<String, ContentHandler>();
	
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.appContext = context;
	}

	public HazelcastInstance getHzInstance() {
		return hzInstance;
	}
	
    //@Autowired
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
		hzInstance.getUserContext().put(ctx_repo, this);
		setPopulationManagement((PopulationManagement) hzInstance.getUserContext().get(ctx_popService));
		logger.debug("setHzInstange; got instance: {}", hzInstance.getName());
	}
	
	@Override
	public void setBindingManagement(BindingManagement bindMgr) {
		super.setBindingManagement(bindMgr);
		((BindingManagementImpl) bindMgr).setRepository(this);
	}

	@Override
	public ClientManagement getClientManagement() {
		return clientMgr;
	}
	
	public void setClientManagement(ClientManagement clientMgr) {
		this.clientMgr = clientMgr;
		((ClientManagementImpl) clientMgr).setRepository(this);
	}
	
	@Override
	public IndexManagement getIndexManagement() {
		return indexMgr;
	}

	public void setIndexManagement(IndexManagement indexMgr) {
		this.indexMgr = indexMgr;
		((IndexManagementImpl) indexMgr).setRepository(this);
	}
	
	public ModelManagement getModelManagement() {
		return modelMgr;
	}

	@Override
	public PopulationManagement getPopulationManagement() {
		return popMgr;
	}

	public void setPopulationManagement(PopulationManagement popMgr) {
		this.popMgr = popMgr;
	}
	
	public void setModelManagement(ModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}
	
	@Override
	public void setTxManagement(TransactionManagement txMgr) {
		super.setTxManagement(txMgr);
		((TransactionManagementImpl) txMgr).setRepository(this);
	}

	@Override
	public TriggerManagement getTriggerManagement() {
		return triggerMgr;
	}

	public void setTriggerManagement(TriggerManagement triggerMgr) {
		this.triggerMgr = triggerMgr;
		((TriggerManagementImpl) triggerMgr).setRepository(this);
	}
	
	@Override
	public String getClientId() {
		return thClient.get();
	}
	
	@Override
	public String getUserName() {
		String user = clientMgr.getCurrentUser();
		//logger.info("getUserName; user: {}", user); 
		if (user == null) {
			user = "unknown";
		}
		return user;
	}
	
	@Override
	public Schema getSchema() {
		return xdmSchema;
	}
	
	public void setSchema(Schema xdmSchema) {
		// TODO: think about run-time updates..
		this.xdmSchema = xdmSchema;
		afterInit();
	}
	
	XQProcessor getXQProcessor() {
		String clientId = thClient.get();
		return getXQProcessor(clientId);
	}
	
	public XQProcessor getXQProcessor(String clientId) {
		XQProcessor result;
		if (clientId == null) {
			result = newXQProcessor();
		} else {
			thClient.set(clientId);
			result = processors.get(clientId);
			if (result == null) {
				result = newXQProcessor();
				processors.put(clientId, result);
			}
		}
		logger.trace("getXQProcessor; returning: {}, factory: {}, repo: {}", 
				result, result.getXQDataFactory(), result.getRepository());
		return result;
	}
	
	private XQProcessor newXQProcessor() {
		XQProcessor result = appContext.getBean(XQProcessor.class, this);
		//XDMQueryManagement qMgr = getQueryManagement();
		//result.setRepository(this);
		return result;
	}
	
	public KeyFactory getFactory() {
		return xdmFactory;
	}
	
	public void setFactory(KeyFactory factory) {
		this.xdmFactory = factory;
	}
	
	private ContentHandler getHandler(String dataFormat) {
		logger.trace("getHandler.enter; got dataFormat: {}", dataFormat); 
		ContentHandler ch = handlers.get(dataFormat);
		if (ch == null) {
			DataFormat df = getDataFormat(dataFormat);
			if (df != null) {
				dataFormat = df.getName();
				ch = handlers.get(dataFormat);
				if (ch == null) {
					ch = instantiateClass(df.getHandlerClass());
				}
			}
			if (ch != null) {
				ch.init(df.getProperties());
				handlers.putIfAbsent(dataFormat, ch);
			}
		}
		logger.trace("getHandler.exit; returning handler {} for dataFormat: {}", ch, dataFormat); 
		return ch;
	}
	
	@Override
	public ContentParser getParser(String dataFormat) {
		ContentHandler ch = getHandler(dataFormat);
		if (ch != null) {
			return ch.getParser();
		}
		return null;
	}
	
	@Override
	public ContentBuilder getBuilder(String dataFormat) {
		ContentHandler ch = getHandler(dataFormat);
		if (ch != null) {
			return ch.getBuilder();
		}
		return null;
	}
	
	@Override
	public ContentModeler getModeler(String dataFormat) {
		ContentHandler ch = getHandler(dataFormat);
		if (ch != null) {
			return ch.getModeler();
		}
		return null;
	}
	
	private <T> T instantiateClass(String className) {
		
		try {
			Class<?> clazz = Class.forName(className);
			return (T) clazz.getConstructor(ModelManagement.class).newInstance(getModelManagement());
		} catch (Exception ex) {
			logger.error("instantiateClass; cannot instantiate: " + className, ex);
		}
		return null;
	}
	
	@Override
	public void close() {
		// TODO: disconnect all clients ?
	}
	
	public DataFormat getDataFormat(String dataFormat) {

		// TODO: make it as fast as possible as it is called from many other places!
		//logger.info("getDataFormat; format: {}", dataFormat);
		Map<String, DataFormat> formats = xdmFormats;
		if (formats == null) {
			HazelcastInstance dataInstance = findSystemInstance();
			if (dataInstance != null) {
				formats = dataInstance.getMap(CN_SYS_FORMATS);
			}
		}
		if (formats != null) {
			// find by name
			DataFormat df = formats.get(dataFormat);
			if (df != null) {
				return df;
			} else {
				// find by extension
				for (DataFormat df2: formats.values()) {
					if (df2.getExtensions().contains(dataFormat)) {
						return df2;
					}
				}
			}
			
			// still not found -> get schema default format
			dataFormat = this.xdmSchema.getProperty(pn_schema_format_default);
			return formats.get(dataFormat);
		}
		return null;
	}

	public void setDataFormats(Collection<DataFormat> cFormats) {
		if (cFormats != null) {
			xdmFormats = new HashMap<>(cFormats.size());
			for (DataFormat df: cFormats) {
				xdmFormats.put(df.getName(), df);
			}
		}
	}
	
	public Collection<Library> getLibraries() {
		if (xdmLibraries != null) {
			return xdmLibraries;
		}
		
		HazelcastInstance dataInstance = findSystemInstance();
		if (dataInstance != null && hasStorageMembers(dataInstance)) {
			Map<String, Library> libraries = dataInstance.getMap(CN_SYS_LIBRARIES);
			return libraries.values();
		}
		return Collections.emptyList(); 
	}

	public void setLibraries(Collection<Library> cLibraries) {
		if (cLibraries != null) {
			xdmLibraries = new ArrayList<>(cLibraries);
		}
	}
	
	public Collection<Module> getModules() {
		if (xdmModules != null) {
			return xdmModules;
		}
		
		HazelcastInstance dataInstance = findSystemInstance();
		if (dataInstance != null && hasStorageMembers(dataInstance)) {
			Map<String, Module> modules = dataInstance.getMap(CN_SYS_MODULES);
			return modules.values();
		}
		return Collections.emptyList(); 
	}
	
	public void setModules(Collection<Module> cModules) {
		if (cModules != null) {
			xdmModules = new ArrayList<>(cModules);
		}
	}
	
	public void afterInit() {
		Set<Index> indexes = xdmSchema.getIndexes();
		if (indexes.size() > 0) {
			for (Index idx: indexes) {
				try {
					indexMgr.createIndex(idx);
				} catch (BagriException ex) {
					logger.warn("afterInit.error; index: " + idx, ex);
				}
			}
		}
		
		// now init triggers..
		Set<TriggerDefinition> triggers = xdmSchema.getTriggers();
		if (triggers.size() > 0) {
			for (TriggerDefinition trg: triggers) {
				triggerMgr.createTrigger(trg);
			}
		}
	}
	
	public boolean isInitialized() {
		if (xdmFormats != null && xdmModules != null && xdmLibraries != null) {
			return true;
		}
		HazelcastInstance sysInstance = findSystemInstance();
		if (sysInstance == null) {
			return false;
		}
		return hasStorageMembers(sysInstance);
	}
	
	public boolean addSchemaIndex(Index index) {
		
		if (xdmSchema.addIndex(index)) {
			Path[] paths;
			try {
				paths = indexMgr.createIndex(index);
			} catch (BagriException ex) {
				logger.warn("addSchemaIndex.error; index: " + index, ex);
				return false;
			}
			
			DocumentManagementImpl docMgr = (DocumentManagementImpl) getDocumentManagement();
			for (Path xPath: paths) {
				try {
					docMgr.indexElements(xPath.getPathId());
				} catch (BagriException ex) {
					logger.warn("addSchemaIndex.error; index: " + index, ex);
				}
			}
			return paths.length > 0;
		}
		logger.info("addSchemaIndex; index {} already exists! do we need to index values?", index);
		return false;
	}

	public boolean dropSchemaIndex(String name) {
		
		Index index = xdmSchema.removeIndex(name);
		if (index != null) {
			Path[] paths;
			try {
				paths = indexMgr.dropIndex(index);
			} catch (BagriException ex) {
				logger.warn("addSchemaIndex.error; index: " + index, ex);
				return false;
			}
			
			DocumentManagementImpl docMgr = (DocumentManagementImpl) getDocumentManagement();
			int cnt = 0;
			List<Integer> pathIds = new ArrayList<>(paths.length);
			for (Path xPath: paths) {
				pathIds.add(xPath.getPathId());
				cnt += docMgr.deindexElements(xPath.getPathId());
			}

			QueryManagementImpl queryMgr = (QueryManagementImpl) getQueryManagement();
			Set<Integer> qKeys = queryMgr.getQueriesForPaths(pathIds, true);
			if (!qKeys.isEmpty()) {
				queryMgr.removeQueries(qKeys);
			}
			
			return cnt > 0;
		}
		logger.info("dropSchemaIndex; index {} does not exist?", index);
		return false;
	}
	
	public boolean addSchemaTrigger(TriggerDefinition trigger) {
		
		if (xdmSchema.addTrigger(trigger)) {
			return triggerMgr.createTrigger(trigger);
		}
		return false;
	}

	public boolean dropSchemaTrigger(String className) {
		
		TriggerDefinition trigger = xdmSchema.removeTrigger(className);
		if (trigger != null) {
			return triggerMgr.deleteTrigger(trigger);
		}
		return false;
	}

	
}

