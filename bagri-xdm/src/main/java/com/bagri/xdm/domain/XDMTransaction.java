package com.bagri.xdm.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bagri.common.util.DateUtils;
import com.bagri.xdm.api.XDMTransactionIsolation;
import com.bagri.xdm.api.XDMTransactionState;

public class XDMTransaction {
	
	private long txId;
	// what about timezone?
	private long startedAt;
	private long finishedAt;
	private String startedBy;
	private XDMTransactionState txState;
	private XDMTransactionIsolation txIsolation;
	private int docsCreated = 0;
	private int docsUpdated = 0;
	private int docsDeleted = 0;

	public XDMTransaction() {
		//
	}

	public XDMTransaction(long txId, String startedBy) {
		this(txId, System.currentTimeMillis(), startedBy);
	}
	
	public XDMTransaction(long txId, long startedAt, String startedBy) {
		this(txId, startedAt, 0, startedBy, XDMTransactionIsolation.readCommited, XDMTransactionState.started);
	}

	public XDMTransaction(long txId, long startedAt, long finishedAt, 
			String startedBy, XDMTransactionIsolation txIsolation, XDMTransactionState txState) {
		this.txId = txId;
		this.startedAt = startedAt;
		this.finishedAt = finishedAt;
		this.startedBy = startedBy;
		this.txIsolation = txIsolation;
		this.txState = txState;
	}
	
	public int getDocsCreated() {
		return docsCreated;
	}
	
	public int getDocsUpdated() {
		return docsUpdated;
	}
	
	public int getDocsDeleted() {
		return docsDeleted;
	}
	
	public long getTxId() {
		return txId;
	}

	public long getStartedAt() {
		return startedAt;
	}

	public long getFinishedAt() {
		return finishedAt;
	}

	public String getStartedBy() {
		return startedBy;
	}

	public XDMTransactionIsolation getTxIsolation() {
		return txIsolation;
	}

	public XDMTransactionState getTxState() {
		return txState;
	}
	
	public void finish(boolean commit) {
		finish(commit, System.currentTimeMillis());
	}
	
	public void finish(boolean commit, long finishedAt) {
		this.finishedAt = finishedAt;
		if (commit) {
			this.txState = XDMTransactionState.commited;
		} else {
			this.txState = XDMTransactionState.rolledback;
		}
	}
	
	public void updateCounters(int created, int updated, int deleted) {
		docsCreated += created;
		docsUpdated += updated;
		docsDeleted += deleted;
	}

	public Map<String, Object> toMap() {
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

	@Override
	public String toString() {
		return "XDMTransaction [txId=" + txId + ", startedAt=" + startedAt
				+ ", finishedAt=" + finishedAt + ", startedBy=" + startedBy
				+ ", txIsolation=" + txIsolation + ", txState=" + txState
				+ ", docsCreated=" + docsCreated + ", docsUpdated=" + docsUpdated
				+ ", docsDeleted=" + docsDeleted + "]";
	}
	
}
