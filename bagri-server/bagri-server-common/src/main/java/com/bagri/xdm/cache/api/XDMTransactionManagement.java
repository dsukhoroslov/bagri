package com.bagri.xdm.cache.api;

import java.util.concurrent.Callable;

public interface XDMTransactionManagement extends com.bagri.xdm.api.XDMTransactionManagement {

	<V> V callInTransaction(long txId, Callable<V> call);	
}
