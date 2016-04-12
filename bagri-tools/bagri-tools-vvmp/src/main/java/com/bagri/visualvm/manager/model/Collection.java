package com.bagri.visualvm.manager.model;

public class Collection implements Comparable<Collection> {
	
	private String name;
	private String description;
	private String createdAt;
	private String createdBy;
	private int id;
	private int version;
	private String docType;
	private boolean enabled;
	
	private int docCount;
	private int eltCount;
	private int fraCount;
	private long byteSize;
	private double avgEltSize;
	private double avgByteSize;
	
	
	public Collection(String name, String description, String createdAt, String createdBy, int id, int version,
			String docType, boolean enabled, int docCount, int eltCount, int fraCount, long byteSize, double avgEltSize,
			double avgByteSize) {
		super();
		this.name = name;
		this.description = description;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.id = id;
		this.version = version;
		this.docType = docType;
		this.enabled = enabled;
		this.docCount = docCount;
		this.eltCount = eltCount;
		this.fraCount = fraCount;
		this.byteSize = byteSize;
		this.avgEltSize = avgEltSize;
		this.avgByteSize = avgByteSize;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the createdAt
	 */
	public String getCreatedAt() {
		return createdAt;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @return the docType
	 */
	public String getDocType() {
		return docType;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @return the docCount
	 */
	public int getDocCount() {
		return docCount;
	}

	/**
	 * @return the eltCount
	 */
	public int getEltCount() {
		return eltCount;
	}

	/**
	 * @return the fraCount
	 */
	public int getFraCount() {
		return fraCount;
	}

	/**
	 * @return the byteSize
	 */
	public long getByteSize() {
		return byteSize;
	}

	/**
	 * @return the avgEltSize
	 */
	public double getAvgEltSize() {
		return avgEltSize;
	}

	/**
	 * @return the avgByteSize
	 */
	public double getAvgByteSize() {
		return avgByteSize;
	}

	@Override
	public String toString() {
		return name + " [" + docCount + "]";
	}

	@Override
	public int compareTo(Collection other) {
		return this.name.compareTo(other.name);
	}

}
