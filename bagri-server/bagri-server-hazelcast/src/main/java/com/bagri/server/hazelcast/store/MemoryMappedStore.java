package com.bagri.server.hazelcast.store;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.IMap;

public abstract class MemoryMappedStore<K, E> {
	
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected static final int szInt = 4;
	
    protected Map<K, Integer> pointers = new HashMap<>();
    private List<FileBuffer> buffers = new ArrayList<>(8);
    protected AtomicInteger cntActive = new AtomicInteger(0);

	protected String dataPath;
	protected String nodeNum;
    protected boolean initialized;
    protected int buffSize; 

	protected abstract String getBufferName(int section);
    protected abstract K getEntryKey(E entry);
    protected abstract int getEntrySize(E entry);
    protected abstract boolean isEntryActive(E entry);
    protected abstract E readEntry(MappedByteBuffer buff);
    protected abstract void writeEntry(MappedByteBuffer buff, E entry);
    protected abstract void deactivateEntry(MappedByteBuffer buff, E entry);
    
    public MemoryMappedStore(String dataPath, String nodeNum, int buffSize) {
		this.dataPath = dataPath;
		this.nodeNum = nodeNum;
		this.buffSize = buffSize;
    }
    
    public void init(IMap<K, E> cache) {
    	//
    	initialized = true;
    }
    
    public void close() {
    	for (FileBuffer fb: buffers) {
    		try {
    			fb.close();
    		} catch (IOException ex) {
    			logger.error("close.error: ", ex);
    		}
    	}
    }
    
    public Collection<K> getEntryKeys() {
    	return pointers.keySet();
    }
    
	public boolean clearEntry(K key, E entry, boolean expectExists) {
		Integer pointer = pointers.get(key);
		if (pointer != null) {
			if (!expectExists) {
				// already exists, but not expected; happens at cache population..
				logger.debug("clearEntry; entry already exists, but not expected; key: {}", key);
				return false;
			}

			int sect = toSection(pointer);
			int pos = toPosition(pointer);
			FileBuffer fb = buffers.get(sect);
			fb.lock();
			try {
				fb.buff.position(pos);
				//mark entry as inactive..
				deactivateEntry(fb.buff, entry);
				cntActive.decrementAndGet();
			} finally {
				fb.unlock();
			}
		} else {
			if (expectExists) {
				// not exists, but expected
				logger.info("clearEntry; entry not found, but expected; key: {}", key);
				return false;
			}
		}
		return true;
	}
	
	public E getEntry(K key) {
		E result = null;
		Integer pointer = pointers.get(key);
		if (pointer != null) {
			int sect = toSection(pointer);
			int pos = toPosition(pointer);
			FileBuffer fb = buffers.get(sect);
			fb.lock();
			try {
				fb.buff.position(pos);
				result = readEntry(fb.buff);
			} finally {
				fb.unlock();
			}
		} else {
			logger.debug("getEntry; cann't find entry for key: {}", key);
		}
		return result;
	}
	
	public boolean putEntry(K key, E entry, boolean expectExists) {
		boolean result = clearEntry(key, entry, expectExists);
		if (!result) {
			return false;
		}
		
		FileBuffer fb = getLockedBuffer(entry);
		try {
			int pos = fb.buff.position();
			int point = toPointer(pos, fb.section);
			pointers.put(key, point);
			int cnt = fb.buff.getInt(0);
			cnt++;
			fb.buff.putInt(0, cnt);
			writeEntry(fb.buff, entry);
			fb.setTail();
			if (isEntryActive(entry)) {
				cntActive.incrementAndGet();
			}
		} finally {
			fb.unlock();
		}
		return true;
	}
	
	//public boolean updateEntry(K key, E entry) {
	//	Integer pointer = pointers.get(key);
	//	if (pointer != null) {
	//		int sect = toSection(pointer);
	//		int pos = toPosition(pointer);
	//		FileBuffer fb = buffers.get(sect);
	//		fb.lock();
	//		try {
	//			fb.buff.position(pos);
	//			writeEntry(fb.buff, entry);
	//			return true;
	//		} finally {
	//			fb.unlock();
	//		}
	//	} else {
	//		logger.debug("updateEntry; cann't find entry for key: {}, putting it as a new one..", key);
	//		return putEntry(key, entry, false);
	//	}
	//}
	
	protected int getSection(String fileName) {
		int pos1 = fileName.indexOf("_") + 1;
		int pos2 = fileName.indexOf(".", pos1);
		return Integer.parseInt(fileName.substring(pos1, pos2));
	}
	
	protected synchronized FileBuffer initBuffer(String fileName, int section) throws IOException {
        FileBuffer fb = new FileBuffer(fileName, buffSize, section); 
        buffers.add(fb);
        int expected = buffers.size() - 1;
        if (expected != section) {
        	logger.warn("initBuffer; added buffer at the wrong position: {}; expected: {}", expected, section);
        }
        fb.reset();
		return fb;
	}
	
	protected int readEntries(FileBuffer fb) {
		logger.trace("readEntries.enter; file buffer: {}", fb);
		int idx = 0;
		int actCount = 0;
		int sect = fb.section;
		fb.buff.position(0);
		int docCount = fb.buff.getInt();
		while (idx < docCount) {
			int pos = fb.buff.position();
			int ptr = toPointer(pos, sect);
			logger.trace("readEntries; reading entry from pos: {}:{} at index: {}; active count:{} from total: {}", pos, ptr, idx, actCount, docCount);
			E entry = readEntry(fb.buff);
			pointers.put(getEntryKey(entry), ptr);
			idx++;
			if (isEntryActive(entry)) {
				actCount++;
			}
		}
		fb.setTail();
		logger.trace("readEntries.exit; active entries: {}; pointers: {}", actCount, pointers.size());
		cntActive.addAndGet(actCount);
		return actCount;
	}
	
	protected int loadEntries(IMap<K, E> cache) {
		logger.info("loadEntries.enter; entry count: {}", cache.size());
		int actCount = 0;
		int sect = 0;
		int cnt = 0;
		FileBuffer fb = null;
		for (E entry: cache.values()) {
			if (fb == null) {
				fb = getLockedBuffer(entry);
			}
			int pos = fb.buff.position();
			int ptr = toPointer(pos, sect);
			logger.trace("loadEntries; writing entry to pos: {}:{} at index: {}; remaining: {}", pos, ptr, cnt, fb.buff.remaining());
			pointers.put(getEntryKey(entry), ptr);
			writeEntry(fb.buff, entry);
			if (isEntryActive(entry)) {
				actCount++;
			}
			cnt++;
			int sz = getEntrySize(entry);
			logger.trace("loadEntries; remaining after write: {}; size: {}", fb.buff.remaining(), sz);
			if (fb.buff.remaining() < sz) {
				fb.setTail();
				fb.buff.putInt(0, cnt);
				fb.unlock();
				fb = null;
				cnt = 0;
			}
		}
		if (fb != null) {
			fb.setTail();
			fb.buff.putInt(0, cnt);
			fb.unlock();
		}
		logger.info("loadEntries.exit; active entries: {}; pointers: {}", actCount, pointers.size());
		cntActive.addAndGet(actCount);
		return actCount;
	}

	private /*synchronized*/ FileBuffer getLockedBuffer(E entry) {
		FileBuffer fb = buffers.get(buffers.size() - 1);
		fb.lock();
        fb.reset();
		int sz = getEntrySize(entry);
		logger.trace("getLockedBuffer; remaining: {}; entry: {}; pos: {}; tail: {}", fb.buff.remaining(), sz, fb.buff.position(), fb.tail);
		if (fb.buff.remaining() < sz) {
			int sect = fb.section + 1;
			String fName = getBufferName(sect);
			fb.unlock();
			try {
				fb = initBuffer(fName, sect);
			} catch (IOException ex) {
				logger.error("getLockedBuffer.error:", ex);
				throw new RuntimeException(ex);
			}
			fb.lock();
		}
		return fb;
	}
	
	private int toPointer(int position, int section) {
		int result = position << 6;
		return result | section;
	}

	private int toPosition(int pointer) {
		return pointer >> 6;
	}
	
	private int toSection(int pointer) {
		return pointer & 0x0000003F;
	}

	protected int[] getIntArray(MappedByteBuffer buff) {
		int len = buff.getInt();
		int[] val = new int[len];
		for (int i=0; i < len; i++) {
			val[i] = buff.getInt();
		}
		return val;
	}

	protected void putIntArray(MappedByteBuffer buff, int[] val) {
		buff.putInt(val.length);
		for (int i=0; i < val.length; i++) {
			buff.putInt(val[i]);
		}
	}
    
	
	protected String getString(MappedByteBuffer buff) {
		int len = buff.getInt();
		if (len > 100) {
			logger.info("getString; too long: {}", len);
		}
		byte[] str = new byte[len];
		buff.get(str);
		return new String(str);
	}

	protected void putString(MappedByteBuffer buff, String val) {
		byte[] str = val.getBytes();
		buff.putInt(str.length);
		buff.put(str);
	}
    
    
    protected class FileBuffer {

    	private volatile int tail;
    	private int section;
    	private Lock lock;
        private FileChannel fc;
    	private RandomAccessFile raf;
        private MappedByteBuffer buff;
        
        FileBuffer(String fileName, int size, int section) throws IOException {
        	this.raf = new RandomAccessFile(fileName, "rw");
    		this.fc = raf.getChannel();
    		size = Math.max(size, (int) raf.length());
    		this.buff = fc.map(MapMode.READ_WRITE, 0, size);
        	// get section from filename ?
        	this.section = section;
        	this.lock = new ReentrantLock(); //true ?
        	this.tail = szInt;
        }
        
        void close() throws IOException {
   			fc.close();
   			raf.close();
        }
        
        int getCount() {
			return buff.getInt(0);
        }
        
        //int getTail() {
        //	return tail;
        //}
        
        void setTail() {
        	tail = buff.position();
        }
        
        void reset() {
        	buff.position(tail);
        }

        void lock() {
        	this.lock.lock();
        }
        
        void unlock() {
        	this.lock.unlock();
        }
    	
        @Override
        public String toString() {
        	return "FileBuffer [section: " + section + "; tail: " + tail +"; position: " + buff.position() + "]";
        }
    }

}
