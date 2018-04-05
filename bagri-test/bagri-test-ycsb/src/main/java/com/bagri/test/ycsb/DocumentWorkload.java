package com.bagri.test.ycsb;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.Workload;
import com.yahoo.ycsb.WorkloadException;

public class DocumentWorkload extends Workload {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentWorkload.class);
	
	@Override
	public void init(Properties props) throws WorkloadException {
		LOGGER.info("init; got props: {}", props);
	}
	
	@Override
	public Object initThread(Properties props, int threadId, int threadCount) throws WorkloadException {
		LOGGER.info("initThread; got props: {}; threadId: {}; threadCount: {}", props, threadId, threadCount);
	    return null;
	}	

	@Override
	public void cleanup() throws WorkloadException {
		LOGGER.info("cleanup");
	}
	  
	@Override
	public boolean doInsert(DB db, Object threadState) {
		LOGGER.info("doInsert; got db: {}; threadState: {}", db, threadState);
		return false;
	}

	@Override
	public boolean doTransaction(DB db, Object threadState) {
		LOGGER.info("doTransaction; got db: {}; threadState: {}", db, threadState);
		return false;
	}

}
