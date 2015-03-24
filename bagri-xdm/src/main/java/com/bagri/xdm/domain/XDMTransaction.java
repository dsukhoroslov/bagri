package com.bagri.xdm.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bagri.common.util.DateUtils;
import com.bagri.xdm.common.XDMTransactionIsolation;
import com.bagri.xdm.common.XDMTransactionState;

public class XDMTransaction {
	
	private long txId;
	// what about timezone?
	private long startedAt;
	private long finishedAt;
	private String startedBy;
	private XDMTransactionState txState;
	private XDMTransactionIsolation txIsolation;

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

	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap<>();
		result.put("txId", txId);
		result.put("started at", new Date(startedAt).toString());
		result.put("started by", startedBy);
		result.put("duration", DateUtils.getDuration(System.currentTimeMillis() - startedAt));
		result.put("isolation", txIsolation.toString());
		result.put("state", txState.toString());
		return result;
	}

	@Override
	public String toString() {
		return "XDMTransaction [txId=" + txId + ", startedAt=" + startedAt
				+ ", finishedAt=" + finishedAt + ", startedBy=" + startedBy
				+ ", txIsolation=" + txIsolation + ", txState=" + txState + "]";
	}
	

}
