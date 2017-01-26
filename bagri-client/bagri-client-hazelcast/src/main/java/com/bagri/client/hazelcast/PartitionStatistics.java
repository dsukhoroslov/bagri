package com.bagri.client.hazelcast;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_PartitionStats;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class PartitionStatistics implements Comparable<PartitionStatistics>, IdentifiedDataSerializable {

	private String address;
	private int partId;
	private int count;
	private long cost;
	private int queue;
	
	public PartitionStatistics() {
		// ser
	}

	public PartitionStatistics(String address, int partId, int count, long cost, int queue) {
		this.address = address;
		this.partId = partId;
		this.count = count;
		this.cost = cost;
		this.queue = queue;
	}
	
	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return the partId
	 */
	public int getPartId() {
		return partId;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @return the cost
	 */
	public long getCost() {
		return cost;
	}

	/**
	 * @return the queue
	 */
	public int getQueue() {
		return queue;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap<>(5);
   		result.put("address", address);
   		result.put("partition", partId);
   		result.put("count", count);
   		result.put("cost", cost);
   		result.put("in queue", queue);
		return result;
	}

	@Override
	public int compareTo(PartitionStatistics o) {
		return Integer.compare(partId, o.partId);
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_PartitionStats;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		address = in.readUTF();
		partId = in.readInt();
		count = in.readInt();
		cost = in.readLong();
		queue = in.readInt();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(address);
		out.writeInt(partId);
		out.writeInt(count);
		out.writeLong(cost);
		out.writeInt(queue);
	}

}
