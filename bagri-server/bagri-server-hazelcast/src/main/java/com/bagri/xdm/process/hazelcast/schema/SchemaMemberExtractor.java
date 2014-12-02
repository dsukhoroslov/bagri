package com.bagri.xdm.process.hazelcast.schema;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_XDMSchemaMemberTask;

import java.util.concurrent.Callable;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

//@SpringAware
public class SchemaMemberExtractor extends SchemaProcessingTask implements Callable<String> { 
	
	public SchemaMemberExtractor() {
		super();
	}

	public SchemaMemberExtractor(String schemaName) {
		super(schemaName);
	}

	@Override
	public String call() throws Exception {
    	logger.trace("call.enter; schema: {}", schemaName);
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(schemaName);
    	Member member = hz.getCluster().getLocalMember();
		String result = member.getUuid();
    	logger.trace("call.exit; returning: {} for member: {}", result, member);
		return result;
	}

	@Override
	public int getId() {
		return cli_XDMSchemaMemberTask;
	}


}
