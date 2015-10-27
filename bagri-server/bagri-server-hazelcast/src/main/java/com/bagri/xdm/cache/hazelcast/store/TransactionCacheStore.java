package com.bagri.xdm.cache.hazelcast.store;

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

import com.bagri.xdm.common.XDMTransactionIsolation;
import com.bagri.xdm.common.XDMTransactionState;
import com.bagri.xdm.domain.XDMTransaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class TransactionCacheStore implements MapStore<Long, XDMTransaction>, MapLoaderLifecycleSupport {

    private static final Logger logger = LoggerFactory.getLogger(TransactionCacheStore.class);

    private int size = 4096;
    private FileChannel fc;
    private MappedByteBuffer buff;
    private Map<Long, Long> transactions = new HashMap<>();
    
    static String getTxLogFile(String dataPath, String nodeNum) {
		return dataPath + "txlog" + nodeNum + ".xdb";
    }
    
	@Override
	public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
		logger.trace("init.enter; properties: {}", properties);
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
			//buff.
			//buff.flip();
			logger.info("init; tx buffer is {}loaded; {}direct", 
					buff.isLoaded() ? "" : "NOT ", buff.isDirect() ? "" : "NOT ");
		} catch (IOException ex) {
			logger.error("init.error", ex);
		}
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
		return null;
	}

	@Override
	public Set<Long> loadAllKeys() {
		logger.trace("loadAllKeys.enter;");
		// load transactions from file here!
		int cnt = 1;
		for (int i=0; i < cnt; i++) {
			int pos = buff.position();
			XDMTransaction xtx = readTx();
			transactions.put(xtx.getTxId(), (long) pos);
		}
		// TODO: synchronize TxIdGen with loaded tx ids!
		Set<Long> result = transactions.keySet(); 
		logger.trace("loadAllKeys.exit; returning: {}", result);
		return result;
	}
	
	private XDMTransaction readTx() {
		long id = buff.getLong();
		long start = buff.getLong();
		long finish = buff.getLong();
		int len = buff.getInt();
		byte[] str = new byte[len];
		buff.get(str);
		String owner = new String(str);
		len = buff.getInt();
		str = new byte[len];
		buff.get(str);
		String isolation = new String(str);
		len = buff.getInt();
		str = new byte[len];
		buff.get(str);
		String state = new String(str);
		return new XDMTransaction(id, start, finish, owner, 
				XDMTransactionIsolation.valueOf(isolation), XDMTransactionState.valueOf(state));
	}

	@Override
	public void delete(Long key) {
		logger.trace("delete.enter; key: {}", key);
		Long deleted = transactions.remove(key);
		logger.trace("delete.exit; deleted: {}", deleted != null);
	}

	@Override
	public void deleteAll(Collection<Long> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys);
		int cnt = 0;
		for (Long key: keys) {
			if (transactions.remove(key) != null) {
				cnt++;
			}
		}
		logger.trace("deleteAll.exit; deleted: {}", cnt);
	}
	
	private void writeTx(XDMTransaction xtx) {
		buff.putLong(xtx.getTxId());
		buff.putLong(xtx.getStartedAt());
		buff.putLong(xtx.getFinishedAt());
		buff.putInt(xtx.getStartedBy().length());
		buff.put(xtx.getStartedBy().getBytes());
		buff.putInt(xtx.getTxIsolation().name().length());
		buff.put(xtx.getTxIsolation().name().getBytes());
		buff.putInt(xtx.getTxState().name().length());
		buff.put(xtx.getTxState().name().getBytes());
		//buff.force();
	}

	@Override
	public void store(Long key, XDMTransaction value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
		long position;
		try {
			position = fc.position();
			//position = buff.position();
			transactions.put(key, position);
			writeTx(value);
		} catch (IOException ex) {
			logger.error("store.error", ex);
			position = -1;
		}
		logger.trace("store.exit; stored tx {} at position: {}", key, position);
	}

	@Override
	public void storeAll(Map<Long, XDMTransaction> entries) {
		logger.trace("storeAll.enter; entries: {}", entries);
		int cnt = 0;
		for (Map.Entry<Long, XDMTransaction> xtx: entries.entrySet()) {
			long position;
			try {
				position = fc.position();
				//position = buff.position();
				transactions.put(xtx.getKey(), position);
				cnt++;
			} catch (IOException ex) {
				logger.error("storeAll.error", ex);
			}
		}
		logger.trace("store.exit; stored {} transactions for {} entries", cnt, entries.size());
	}

}



