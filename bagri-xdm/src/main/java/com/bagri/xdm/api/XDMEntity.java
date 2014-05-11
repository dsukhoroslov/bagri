package com.bagri.xdm.api;

import java.util.Date;

public abstract class XDMEntity implements Versionable {
	
	private int version;
	private Date createdAt;
	private String createdBy;
	
	public XDMEntity() {
		// ...
	}
	
	public XDMEntity(int version, Date createdAt, String createdBy) {
		this.version = version;
		// todo: think about other Date implementation, joda date, for instance..
		this.createdAt = new Date(createdAt.getTime());
		this.createdBy = createdBy;
	}

	@Override
	public int getVersion() {
		return version;
	}

	@Override
	public Date getCreatedAt() {
		return new Date(createdAt.getTime());
	}

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	@Override
	public void updateVersion(String by) {
		version++;
		createdAt = new Date();
		createdBy = by;
	}

}
