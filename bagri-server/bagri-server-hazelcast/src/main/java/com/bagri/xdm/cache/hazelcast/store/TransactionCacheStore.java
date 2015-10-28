package com.bagri.xdm.cache.hazelcast.store;

import static com.bagri.common.config.XDMConfigConstants.xdm_schema_name;
import static com.bagri.common.config.XDMConfigConstants.xdm_schema_store_data_path;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.cache.hazelcast.impl.TransactionManagementImpl;
import com.bagri.xdm.cache.hazelcast.util.SpringContextHolder;
import com.bagri.xdm.common.XDMTransactionIsolation;
import com.bagri.xdm.common.XDMTransactionState;
import com.bagri.xdm.domain.XDMTransaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class TransactionCacheStore implements MapStore<Long, XDMTransaction>, MapLoaderLifecycleSupport {

    private static final Logger logger = LoggerFactory.getLogger(TransactionCacheStore.class);

    private int txCount = 0;
    private FileChannel fc;
    private String schemaName;
    private MappedByteBuffer buff;
    private Map<Long, Long> transactions;
    
    // TODO: this is for tests only, remove it when proper way to get 
    // MapStore instance will be found
    static TransactionCacheStore instance;
    
    static String getTxLogFile(String dataPath, String nodeNum) {
		return dataPath + "txlog" + nodeNum + ".xdb";
    }
    
	@Override
	public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
		logger.trace("init.enter; properties: {}", properties);
		schemaName = properties.getProperty(xdm_schema_name);
		String dataPath = properties.getProperty(xdm_schema_store_data_path);
		if (dataPath == null) {
			dataPath = "";
		} else {
			dataPath += "/";
		}
		String nodeNum = properties.getProperty("xdm.node.instance");
		if (nodeNum == null) {
			nodeNum = "0";
		}
	    int size = 4096;
		String buffSize = properties.getProperty("xdm.tx.buffer.size");
		if (buffSize != null) {
			size = Integer.parseInt(buffSize);
		}
		String fileName = getTxLogFile(dataPath, nodeNum);
		logger.info("init; opening tx log from file: {}; buffer size: {}", fileName, size);
		
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(fileName, "rw");
			if (raf.length() > 0) {
				logger.info("init; opened tx log with length: {}", raf.length());
				if (raf.length() > size) {
					size = (int) raf.length();
				}
			}
			fc = raf.getChannel();
			buff = fc.map(MapMode.READ_WRITE, 0, size);
			//buff.flip();
			if (raf.length() > 0) {
				txCount = buff.getInt();
				transactions = new HashMap<>(txCount);
			} else {
				transactions = new HashMap<>();
			}
			logger.info("init; tx buffer is {}loaded; tx count: {}", buff.isLoaded() ? "" : "NOT ", txCount);
		} catch (IOException ex) {
			logger.error("init.error", ex);
		}
		instance = this;
	}

	@Override
	public void destroy() {
		logger.trace("destroy.enter;");
		try {
			//buff.compact();
			fc.close();
		} catch (IOException ex) {
			logger.error("destroy.error", ex);
		}
	}
	
	public int getStoredCount() {
		return txCount;
	}
	
	public int getStoredSize() {
		return transactions.size();
	}
	
	@Override
	public XDMTransaction load(Long key) {
		logger.trace("load.enter; key: {}", key);
		XDMTransaction result = null;
		Long position = transactions.get(key);
		if (position != null) {
			// read tx from file..
			buff.position(position.intValue());
			result = readTx();
		}
		logger.trace("load.exit; returning: {}", result);
		return result;
	}

	@Override
	public Map<Long, XDMTransaction> loadAll(Collection<Long> keys) {
		logger.trace("loadAll.enter; keys: {}", keys);
		Map<Long, XDMTransaction> result = new HashMap<>(keys.size());
		for (Long key: keys) {
			Long position = transactions.get(key);
			if (position != null) {
				// read tx from file..
				buff.position(position.intValue());
				XDMTransaction xtx = readTx();
				result.put(key, xtx);
			}
		}
		logger.trace("loadAll.exit; returning: {}", result);
		return result;
	}

	@Override
	public Set<Long> loadAllKeys() {
		logger.trace("loadAllKeys.enter;");
		int idx = 0;
		while (idx < txCount) {
			int pos = buff.position();
			// read just state here!
			XDMTransaction xtx = readTx();
			if (XDMTransactionState.commited != xtx.getTxState()) {
				transactions.put(xtx.getTxId(), (long) pos);
				idx++;
			}
		}
		Set<Long> result = transactions.keySet(); 
		logger.trace("loadAllKeys.exit; returning: {}", result);
		return result;
	}
	
	@Override
	public void delete(Long key) {
		logger.trace("delete.enter; key: {}", key);
		Long position = transactions.remove(key);
		if (position != null) {
			buff.put(position.intValue(), (byte) XDMTransactionState.commited.ordinal());
			txCount--;
			buff.putInt(0, txCount);
		}
		logger.trace("delete.exit; deleted: {}", position != null);
	}

	@Override
	public void deleteAll(Collection<Long> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys);
		int cnt = 0;
		for (Long key: keys) {
			Long position = transactions.remove(key);
			if (position != null) {
				buff.put(position.intValue(), (byte) XDMTransactionState.commited.ordinal());
				txCount--;
				buff.putInt(0, txCount);
				cnt++;
			}
		}
		logger.trace("deleteAll.exit; deleted: {}", cnt);
	}
	
	@Override
	public void store(Long key, XDMTransaction value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
		long position;
		//try {
			//position = fc.position();
			position = buff.position();
			if (transactions.put(key, position) == null) {
				txCount++;
				buff.putInt(0, txCount);
			}
			writeTx(value);
		//} catch (IOException ex) {
		//	logger.error("store.error", ex);
		//	position = -1;
		//}
		logger.trace("store.exit; stored tx {} at position: {}", key, position);
	}

	@Override
	public void storeAll(Map<Long, XDMTransaction> entries) {
		logger.trace("storeAll.enter; entries: {}", entries);
		int cnt = 0;
		for (Map.Entry<Long, XDMTransaction> xtx: entries.entrySet()) {
			long position;
			//try {
				//position = fc.position();
				position = buff.position();
				if (transactions.put(xtx.getKey(), position) == null) {
					txCount++;
					buff.putInt(0, txCount);
				}
				writeTx(xtx.getValue());
				cnt++;
			//} catch (IOException ex) {
			//	logger.error("storeAll.error", ex);
			//}
		}
		logger.trace("store.exit; stored {} transactions for {} entries", cnt, entries.size());
	}
	
	private static final int byLen = 38;
	private static final int txLen = 64;

	private XDMTransaction readTx() {
		XDMTransactionState state = XDMTransactionState.values()[buff.get()];
		long id = buff.getLong();
		long start = buff.getLong();
		long finish = buff.getLong();
		XDMTransactionIsolation isol = XDMTransactionIsolation.values()[buff.get()];
		byte[] by38 = new byte[byLen];
		buff.get(by38);
		int sz = 0;
		while (sz < byLen && by38[sz] > 0) {
			sz++;
		}
		String owner = new String(by38, 0, sz);
		return new XDMTransaction(id, start, finish, owner, isol, state);  
	}

	private void writeTx(XDMTransaction xtx) {
		buff.put((byte) xtx.getTxState().ordinal());
		buff.putLong(xtx.getTxId());
		buff.putLong(xtx.getStartedAt());
		buff.putLong(xtx.getFinishedAt());
		buff.put((byte) xtx.getTxIsolation().ordinal());
		byte[] by = xtx.getStartedBy().getBytes();
		byte[] by38 = new byte[byLen];
		for (int i=0; i < by.length; i++) {
			by38[i] = by[i];
		}
		buff.put(by38);
		//buff.force();
	}

}



