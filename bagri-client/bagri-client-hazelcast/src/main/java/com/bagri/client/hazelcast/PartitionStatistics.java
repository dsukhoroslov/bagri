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
	private int docCount;
	private long docCost;
	private int docInQueue;
	private int docActive;
	private long conCost;
	private int eltCount;
	private long eltCost; 
	private int idxCount;
	private long idxCost;
	private int resCount;
	private long resCost;
	
	public PartitionStatistics() {
		// ser
	}

	public PartitionStatistics(String address, int partId, int docCount, long docCost, int docInQueue, int docActive, long conCost, 
			int eltCount, long eltCost, int idxCount, long idxCost, int resCount, long resCost) {
		this.address = address;
		this.partId = partId;
		this.docCount = docCount;
		this.docCost = docCost;
		this.docInQueue = docInQueue;
		this.docActive = docActive;
		this.conCost = conCost;
		this.eltCount = eltCount;
		this.eltCost = eltCost;
		this.idxCount = idxCount;
		this.idxCost = idxCost;
		this.resCount = resCount;
		this.resCost = resCost;
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
	 * @return the document count
	 */
	public int getDocumentCount() {
		return docCount;
	}

	/**
	 * @return the document cost
	 */
	public long getDocumentCost() {
		return docCost;
	}

	/**
	 * @return the updating document count
	 */
	public int getQueuedDocumentCount() {
		return docInQueue;
	}

	/**
	 * @return the active count
	 */
	public int getActiveDocumentCount() {
		return docActive;
	}

	/**
	 * @return the content cost
	 */
	public long getContentCost() {
		return conCost;
	}

	/**
	 * @return the element count
	 */
	public int getElementCount() {
		return eltCount;
	}

	/**
	 * @return the element cost
	 */
	public long getElementCost() {
		return eltCost;
	}

	/**
	 * @return the index count
	 */
	public int getIndexCount() {
		return idxCount;
	}

	/**
	 * @return the index cost
	 */
	public long getIndexCost() {
		return idxCost;
	}

	/**
	 * @return the result count
	 */
	public int getResultCount() {
		return resCount;
	}

	/**
	 * @return the result cost
	 */
	public long getResultCost() {
		return resCost;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap<>(5);
   		result.put("address", address);
   		result.put("partition", partId);
   		result.put("document count", docCount);
   		result.put("document cost", docCost);
   		result.put("in queue", docInQueue);
   		result.put("active count", docActive);
   		result.put("content cost", conCost);
   		result.put("element count", eltCount);
   		result.put("element cost", eltCost);
   		result.put("index count", idxCount);
   		result.put("index cost", idxCost);
   		result.put("result count", resCount);
   		result.put("result cost", resCost);
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
		docCount = in.readInt();
		docCost = in.readLong();
		docInQueue = in.readInt();
		docActive = in.readInt();
		conCost = in.readLong();
		eltCount = in.readInt();
		eltCost = in.readLong();
		idxCount = in.readInt();
		idxCost = in.readLong();
		resCount = in.readInt();
		resCost = in.readLong();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(address);
		out.writeInt(partId);
		out.writeInt(docCount);
		out.writeLong(docCost);
		out.writeInt(docInQueue);
		out.writeInt(docActive);
		out.writeLong(conCost);
		out.writeInt(eltCount);
		out.writeLong(eltCost);
		out.writeInt(idxCount);
		out.writeLong(idxCost);
		out.writeInt(resCount);
		out.writeLong(resCost);
	}

}
