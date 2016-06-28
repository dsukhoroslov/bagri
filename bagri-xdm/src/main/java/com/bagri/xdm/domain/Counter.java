package com.bagri.xdm.domain;

/**
 * A counter of documents affected by transaction 
 * 
 * @author Denis Sukhoroslov
 *
 */
public class Counter {
	
	private boolean commit;
	private int created;
	private int updated;
	private int deleted;
	
	/**
	 * XDM Counter constructor
	 * 
	 * @param commit commit (true) or rollback (false) an owning transaction 
	 * @param created the number of documents created
	 * @param updated the number of documents updated
	 * @param deleted the number of documents deleted
	 */
	public Counter(boolean commit, int created, int updated, int deleted) {
		this.commit = commit;
		this.created = created;
		this.updated = updated;
		this.deleted = deleted;
	}

	/**
	 * 
	 * @return commit/rollback flag
	 */
	public boolean isCommit() {
		return commit;
	}

	/**
	 * 
	 * @return the number of documents created in transaction
	 */
	public int getCreated() {
		return created;
	}

	/**
	 * 
	 * @return the number of documents updated in transaction
	 */
	public int getUpdated() {
		return updated;
	}

	/**
	 * 
	 * @return the number of documents deleted in transaction
	 */
	public int getDeleted() {
		return deleted;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Counter [commit=" + commit + ", created=" + created
				+ ", updated=" + updated + ", deleted=" + deleted + "]";
	}
	
}
