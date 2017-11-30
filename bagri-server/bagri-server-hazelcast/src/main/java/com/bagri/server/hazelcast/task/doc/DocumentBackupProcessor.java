package com.bagri.server.hazelcast.task.doc;

import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_DocumentBackupProcessor;

import java.io.IOException;
import java.util.Properties;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.DocumentKey;
import com.bagri.core.model.Document;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.impl.MapService;
import com.hazelcast.map.impl.MapServiceContext;
import com.hazelcast.map.impl.recordstore.RecordStore;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentBackupProcessor implements EntryBackupProcessor<DocumentKey, Document>, HazelcastInstanceAware, IdentifiedDataSerializable {
	
	private static final transient Logger logger = LoggerFactory.getLogger(DocumentBackupProcessor.class);
	
    private NodeEngine nodeEngine;
	
	private Document doc;
	private Object content;
	private Properties props;
	
	public DocumentBackupProcessor() {
		//de-ser
	}

	public DocumentBackupProcessor(Document doc, Object content, Properties props) {
		this.doc = doc;
		this.content = content;
		this.props = props;
	}

	@Override
	public void setHazelcastInstance(HazelcastInstance hzInstance) {
		logger.trace("setHazelcastInstance.enter; this: {}; hzInstance: {}", this, hzInstance.getClass().getName());
		this.nodeEngine = ((com.hazelcast.instance.HazelcastInstanceImpl) hzInstance).node.nodeEngine;
	}

	@Override
	public void processBackup(Entry<DocumentKey, Document> entry) {
		logger.trace("processBackup.enter; this: {}; entry: {}", this, entry);
		entry.setValue(doc);
		//RecordStore<?> rs = getRecordStore(entry.getKey(), CN_XDM_CONTENT);
		//Data dKey = nodeEngine.toData(entry.getKey());
        //rs.putBackup(dKey, content);
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_DocumentBackupProcessor;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		doc = in.readObject();
		content = in.readObject();
		props = in.readObject();
		logger.trace("readData.exit; this: {}", this);
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(doc);
		out.writeObject(content);
		out.writeObject(props);
		logger.trace("writeData.exit; this: {}", this);
	}

    //private Map.Entry createMapEntry(Object key, Object value) {
	//	Data dKey = nodeEngine.toData(key);
    //  InternalSerializationService serializationService = ((InternalSerializationService) nodeEngine.getSerializationService());
	//	MapService svc = nodeEngine.getService(MapService.SERVICE_NAME);
	//	MapServiceContext mapCtx = svc.getMapServiceContext();
    //  return new LazyMapEntry(dKey, value, serializationService, mapCtx.getExtractors(CN_XDM_CONTENT));
    //}

	public RecordStore<?> getRecordStore(Object key, String storeName) {
		int partId = nodeEngine.getPartitionService().getPartitionId(key); 
		MapService mapSvc = nodeEngine.getService(MapService.SERVICE_NAME);
		MapServiceContext mapCtx = mapSvc.getMapServiceContext();
		return mapCtx.getRecordStore(partId, storeName);
	}
	
}
