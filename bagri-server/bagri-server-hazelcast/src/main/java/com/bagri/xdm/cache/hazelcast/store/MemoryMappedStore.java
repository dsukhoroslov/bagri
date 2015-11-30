package com.bagri.xdm.cache.hazelcast.store;

import static com.bagri.common.util.FileUtils.buildSectionFileName;

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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.IMap;

public abstract class MemoryMappedStore<K, E> {
	
    protected final Logger logger = LoggerFactory.getLogger(getClass());
	
    private Map<K, Integer> pointers = new HashMap<>();
    private List<FileBuffer> buffers = new ArrayList<>(8);

	protected String dataPath;
	protected String nodeNum;
    protected boolean initialized;
    protected int buffSize = 2048*100;

    public abstract K getEntryKey(E entry);
    public abstract int getEntrySize(E entry);
    public abstract E readEntry(MappedByteBuffer buff);
    public abstract void writeEntry(MappedByteBuffer buff, E entry);
    
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
    			// just log it..
    		}
    	}
    }
    
    public Collection<K> getEntryKeys() {
    	return pointers.keySet();
    }
    
	public E getEntry(K key) {
		E result = null;
		Integer pointer = pointers.get(key);
		if (pointer != null) {
			int sect = toSection(pointer);
			FileBuffer fb = buffers.get(sect);
			if (fb != null) {
				int pos = toPosition(pointer);
				fb.buff.position(pos);
				result = readEntry(fb.buff);
			} else {
				//
			}
		} else {
			//
		}
		return result;
	}
	
	public boolean putEntry(K key, E entry, boolean acceptExisting) {
		FileBuffer fb;
		Integer pointer = pointers.get(key);
		if (pointer != null) {
			if (!acceptExisting) {
				// already exists
				//logger.debug("entryAdded; document already exists: {}", event.getKey());
				return false;
			}

			int sect = toSection(pointer);
			fb = buffers.get(sect);
			if (fb != null) {
				int pos = toPosition(pointer);
				fb.buff.position(pos);
				//writeEntry(fb.buff, entry);
				//mark entry as inactive..
				//buff.putLong(txFinish);
			} else {
				//
			}
		} else {
			//
		}

		fb = getLockedBuffer(entry);
		try {
			fb.buff.reset();
			int pos = fb.buff.position();
			int point = toPointer(pos, fb.section);
			pointers.put(key, point);
			int cnt = fb.buff.getInt(0);
			cnt++;
			fb.buff.putInt(0, cnt);
			writeEntry(fb.buff, entry);
			fb.buff.mark();
		} finally {
			fb.unlock();
		}
		return true;
/*		
		Long pos = documents.get(event.getKey().getKey());
		if (pos != null) {
			buff.position(pos.intValue());
			long txFinish = event.getValue().getTxFinish(); 
			if (txFinish > 0) {
				buff.putLong(txFinish);
			} else {
				// just mark the doc as inactive
				synchronized (buff) {
					buff.putLong(-1);
					buff.reset();
				//entryAdded(event);
					long pos2 = buff.position();
					documents.put(event.getKey().getKey(), pos2);
					buff.putInt(0, documents.size());
					buff.reset();
					writeDocument(event.getValue());
					buff.mark();
				}
			}
		} else {
			logger.info("entryUpdated; unknown document: {}", event.getKey());
		}
*/		
	}
	
	protected FileBuffer initBuffer(String fileName, int section) throws IOException {
        FileBuffer fb = new FileBuffer(fileName, buffSize, section); 
		return fb;
	}
	
	protected int readEntries(FileBuffer fb) {
		//logger.trace("readDocuments.enter; docCount: {}", docCount);
		int idx = 0;
		int actCount = 0;
		int sect = fb.section;
		int docCount = fb.buff.getInt(0);
		//pointers.clear();
		//buff.position(szInt);
		while (idx < docCount) {
			int pos = fb.buff.position();
			pos = toPointer(pos, sect);
			E entry = readEntry(fb.buff);
			pointers.put(getEntryKey(entry), pos);
			idx++;
			//if (xdoc.getTxFinish() == 0) {
			//	actCount++;
			//}
		}
		//buff.position(nextBit()); 
		fb.buff.mark();
		//logger.trace("readDocuments.exit; active docs: {}; documents: {}", actCount, documents);
		return actCount;
	}
	
	protected int loadEntries(IMap<K, E> cache) {
		//logger.trace("loadDocuments.enter; docCount: {}", xddCache.size());
		int actCount = 0;
		int sect = 0;
		FileBuffer fb = null;
		for (E entry: cache.values()) {
			if (fb == null) {
				fb = getLockedBuffer(entry);
			}
			int pos = fb.buff.position();
			pos = toPointer(pos, sect);
			pointers.put(getEntryKey(entry), pos);
			writeEntry(fb.buff, entry);
			//if (xdoc.getTxFinish() == 0) {
			//	actCount++;
			//}
			if (fb.buff.remaining() < getEntrySize(entry)) {
				//buff.putInt(0, pointers.size());
				fb.buff.mark();
				fb.unlock();
				fb = null;
			}
		}
		//logger.trace("loadDocuments.exit; active docs: {}; documents: {}", actCount, documents);
		return actCount;
	}
	
	private synchronized FileBuffer getLockedBuffer(E entry) {
		FileBuffer fb = buffers.get(buffers.size() - 1);
		if (fb.buff.remaining() < getEntrySize(entry)) {
			int sect = fb.section + 1;
			String fName = getBufferName(sect);
			try {
				fb = new FileBuffer(fName, buffSize, sect);
			} catch (IOException ex) {
				// log
				throw new RuntimeException(ex);
			}
		}
		fb.lock();
		return fb;
	}
	
	private String getBufferName(int section) {
		return buildSectionFileName(dataPath, nodeNum, "catalog", section);
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

	protected String getString(MappedByteBuffer buff) {
		int len = buff.getInt();
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

    	private int section;
    	private Lock lock;
        private FileChannel fc;
    	private RandomAccessFile raf;
        protected MappedByteBuffer buff;
        
        FileBuffer(String fileName, int size, int section) throws IOException {

        	this.raf = new RandomAccessFile(fileName, "rw");
    		this.fc = raf.getChannel();
    		if (raf.length() < size) {
    			size = (int) raf.length();
    		}
    		this.buff = fc.map(MapMode.READ_WRITE, 0, size);
        	// get section from filename ?
        	this.section = section;
        	this.lock = new ReentrantLock(); //true ?  
        }
        
        void close() throws IOException {
   			fc.close();
   			raf.close();
        }

        void lock() {
        	this.lock.lock();
        }
        
        void unlock() {
        	this.lock.unlock();
        }
    	
    }

}
