package com.bagri.xdm.domain;

import com.bagri.xdm.common.XDMTransactionState;

public class XDMTransaction {
	
	private long txId;
	// what about timezone?
	private long startedAt;
	private long finishedAt;
	private String startedBy;
	private XDMTransactionState txState;

	public XDMTransaction() {
		//
	}

	public XDMTransaction(long txId, String startedBy) {
		this(txId, System.currentTimeMillis(), startedBy);
	}
	
	public XDMTransaction(long txId, long startedAt, String startedBy) {
		this(txId, startedAt, 0, startedBy, XDMTransactionState.started);
	}

	public XDMTransaction(long txId, long startedAt, long finishedAt, 
			String startedBy, XDMTransactionState txState) {
		this.txId = txId;
		this.startedAt = startedAt;
		this.finishedAt = finishedAt;
		this.startedBy = startedBy;
		this.txState = txState;
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

	@Override
	public String toString() {
		return "XDMTransaction [txId=" + txId + ", startedAt=" + startedAt
				+ ", finishedAt=" + finishedAt + ", startedBy=" + startedBy
				+ ", txState=" + txState + "]";
	}
	

}
