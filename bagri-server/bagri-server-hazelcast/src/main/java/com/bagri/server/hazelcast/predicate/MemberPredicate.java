package com.bagri.server.hazelcast.predicate;

import java.util.Map;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;
import com.hazelcast.query.Predicate;

public class MemberPredicate<K, V> implements Predicate<K, V> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7718023577992911275L;
	
	private PartitionService partService;
	private Member member;
	private Predicate<K, V> predicate;
	
	public MemberPredicate() {
		//
	}
	
	public MemberPredicate(PartitionService partService, Member member, Predicate<K, V> predicate) {
		this.partService = partService;
		this.member = member;
		this.predicate = predicate;
	}

	@Override
	public boolean apply(Map.Entry<K, V> mapEntry) {
		if (member.equals(partService.getPartition(mapEntry.getKey()).getOwner())) {
			return predicate.apply(mapEntry);
		}
		return false;
	}

}
