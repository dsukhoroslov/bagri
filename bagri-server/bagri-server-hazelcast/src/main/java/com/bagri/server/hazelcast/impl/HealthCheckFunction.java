package com.bagri.server.hazelcast.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Member;
import com.hazelcast.quorum.QuorumFunction;

public class HealthCheckFunction implements QuorumFunction {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckFunction.class);
	
	@Override
	public boolean apply(Collection<Member> members) {
		logger.info("apply; members: {}", members);
		return false;
	}

}
