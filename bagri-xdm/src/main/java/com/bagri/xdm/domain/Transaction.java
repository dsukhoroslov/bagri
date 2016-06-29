package com.bagri.xdm.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bagri.common.Convertable;
import com.bagri.common.util.DateUtils;
import com.bagri.xdm.api.TransactionIsolation;
import com.bagri.xdm.api.TransactionState;

/**
 * Represents cached transaction record
 * 
 * @author Denis Sukhoroslov
 *
 */
public class Transaction implements Convertable<Map<String, Object>> {
	
	private long txId;
	// what about timezone?
	private long startedAt;
	private long finishedAt;
	private String startedBy;
	private TransactionState txState;
	private TransactionIsolation txIsolation;
	private int docsCreated = 0;
	private int docsUpdated = 0;
	private int docsDeleted = 0;

	/**
	 * default constructor
	 */
	public Transaction() {
		//
	}

	/**
	 * 
	 * @param txId transaction id
	 * @param startedBy transaction owner: the user who has started the transaction
	 */
	public Transaction(long txId, String startedBy) {
		this(txId, System.currentTimeMillis(), startedBy);
	}
	
	/**
	 * 
	 * @param txId transaction id
	 * @param startedAt the date/time when transaction has been started
	 * @param startedBy transaction owner: the user who has started the transaction
	 */
	public Transaction(long txId, long startedAt, String startedBy) {
		this(txId, startedAt, 0, startedBy, TransactionIsolation.readCommited, TransactionState.started);
	}

	/**
	 * 
	 * @param txId transaction id
	 * @param startedAt the date/time when transaction has been started
	 * @param finishedAt the date/time when transaction has been finished
	 * @param startedBy transaction owner: the user who has started the transaction
	 * @param txIsolation transaction isolation level
	 * @param txState transaction state
	 */
	public Transaction(long txId, long startedAt, long finishedAt, 
			String startedBy, TransactionIsolation txIsolation, TransactionState txState) {
		this.txId = txId;
		this.startedAt = startedAt;
		this.finishedAt = finishedAt;
		this.startedBy = startedBy.intern();
		this.txIsolation = txIsolation;
		this.txState = txState;
	}
	
	/**
	 * 
	 * @return the number of documents created in this transaction
	 */
	public int getDocsCreated() {
		return docsCreated;
	}
	
	/**
	 * 
	 * @return the number of documents updated in this transaction
	 */
	public int getDocsUpdated() {
		return docsUpdated;
	}
	
	/**
	 * 
	 * @return the number of documents deleted in this transaction
	 */
	public int getDocsDeleted() {
		return docsDeleted;
	}
	
	/**
	 * 
	 * @return the transaction id
	 */
	public long getTxId() {
		return txId;
	}

	/**
	 * 
	 * @return the date/time when transaction has been started
	 */
	public long getStartedAt() {
		return startedAt;
	}

	/**
	 * 
	 * @return the date/time when transaction has been finished
	 */
	public long getFinishedAt() {
		return finishedAt;
	}

	/**
	 * 
	 * @return transaction owner: the user who has started the transaction
	 */
	public String getStartedBy() {
		return startedBy;
	}

	/**
	 * 
	 * @return the transaction isolation level
	 */
	public TransactionIsolation getTxIsolation() {
		return txIsolation;
	}

	/**
	 * 
	 * @return the transaction state
	 */
	public TransactionState getTxState() {
		return txState;
	}
	
	/**
	 * to finish transaction
	 * 
	 * @param commit to commit (true) or rollback (false) the transaction
	 */
	public void finish(boolean commit) {
		finish(commit, System.currentTimeMillis());
	}
	
	/**
	 * to finish transaction
	 * 
	 * @param commit to commit (true) or rollback (false) the transaction
	 * @param finishedAt the date/time when transaction is finished
	 */
	public void finish(boolean commit, long finishedAt) {
		this.finishedAt = finishedAt;
		if (commit) {
			this.txState = TransactionState.commited;
		} else {
			this.txState = TransactionState.rolledback;
		}
	}
	
	/**
	 * updates transaction document counters
	 * 
	 * @param created to add the number of documents created in this transaction 
	 * @param updated to add the number of documents updated in this transaction
	 * @param deleted to add the number of documents deleted in this transaction
	 */
	public void updateCounters(int created, int updated, int deleted) {
		docsCreated += created;
		docsUpdated += updated;
		docsDeleted += deleted;
	}

	/**
	 * {@inheritDoc} 
	 */
	public Map<String, Object> convert() {
		Map<String, Object> result = new HashMap<>();
		result.put("txId", txId);
		result.put("started at", new Date(startedAt).toString());
		result.put("started by", startedBy);
		long finished;
		if (finishedAt > 0) {
			finished = finishedAt;
		} else {
			finished = System.currentTimeMillis();
		}
		result.put("duration", DateUtils.getDuration(finished - startedAt));
		result.put("isolation", txIsolation.toString());
		result.put("state", txState.toString());
		result.put("docs created", docsCreated);
		result.put("docs updated", docsUpdated);
		result.put("docs deleted", docsDeleted);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Transaction [txId=" + txId + ", startedAt=" + startedAt
				+ ", finishedAt=" + finishedAt + ", startedBy=" + startedBy
				+ ", txIsolation=" + txIsolation + ", txState=" + txState
				+ ", docsCreated=" + docsCreated + ", docsUpdated=" + docsUpdated
				+ ", docsDeleted=" + docsDeleted + "]";
	}
	
}
