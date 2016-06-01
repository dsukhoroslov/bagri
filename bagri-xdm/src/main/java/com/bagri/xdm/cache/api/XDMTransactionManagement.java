package com.bagri.xdm.cache.api;

import java.util.concurrent.Callable;

import com.bagri.xdm.api.XDMException;

public interface XDMTransactionManagement extends com.bagri.xdm.api.XDMTransactionManagement {

	<V> V callInTransaction(long txId, boolean readOnly, Callable<V> call) throws XDMException;	

}
