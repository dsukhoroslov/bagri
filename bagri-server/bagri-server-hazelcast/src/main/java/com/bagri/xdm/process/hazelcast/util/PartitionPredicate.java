package com.bagri.xdm.process.hazelcast.util;

import java.util.Map;
import java.util.Set;

import com.hazelcast.core.PartitionService;
import com.hazelcast.query.Predicate;

public class PartitionPredicate<K, V> implements Predicate<K, V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5033489770883082993L;
	
	private PartitionService partService;
	private Set<Integer> partitions;
	private Predicate<K, V> predicate;
	
	public PartitionPredicate() {
		//
	}
	
	public PartitionPredicate(PartitionService partService, Set<Integer> partitions, Predicate<K, V> predicate) {
		this.partService = partService;
		this.partitions = partitions;
		this.predicate = predicate;
	}

	@Override
	public boolean apply(Map.Entry<K, V> mapEntry) {
		if (partitions.contains(partService.getPartition(mapEntry.getKey()).getPartitionId())) {
			return predicate.apply(mapEntry);
		}
		return false;
	}

}
