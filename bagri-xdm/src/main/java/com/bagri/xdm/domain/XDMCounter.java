package com.bagri.xdm.domain;

public class XDMCounter {
	
	private boolean commit;
	private int created;
	private int updated;
	private int deleted;
	
	public XDMCounter(boolean commit, int created, int updated, int deleted) {
		this.commit = commit;
		this.created = created;
		this.updated = updated;
		this.deleted = deleted;
	}

	public boolean isCommit() {
		return commit;
	}

	public int getCreated() {
		return created;
	}

	public int getUpdated() {
		return updated;
	}

	public int getDeleted() {
		return deleted;
	}

	@Override
	public String toString() {
		return "XDMCounter [commit=" + commit + ", created=" + created
				+ ", updated=" + updated + ", deleted=" + deleted + "]";
	}
	
}
