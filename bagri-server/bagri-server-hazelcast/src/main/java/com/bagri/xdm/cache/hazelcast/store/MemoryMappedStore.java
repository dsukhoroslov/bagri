package com.bagri.xdm.cache.hazelcast.store;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hazelcast.core.IMap;

public abstract class MemoryMappedStore<K, E> {
	
	private String catalog;
    private int bSize = 2048;
    private Map<K, Integer> pointers = new HashMap<>();
    private List<FileBuffer> buffers = new ArrayList<>(8);

    public abstract K getEntryKey(E entry);
    public abstract E readEntry(MappedByteBuffer buff);
    public abstract void writeEntry(MappedByteBuffer buff, E entry);
    
	private int readEntries(FileBuffer fb, int count) {
		//logger.trace("readDocuments.enter; docCount: {}", docCount);
		int idx = 0;
		int actCount = 0;
		int sect = fb.getSection();
		pointers.clear();
		MappedByteBuffer buff = fb.buff;
		//buff.position(szInt);
		while (idx < count) {
			int pos = buff.position();
			pos = toPointer(pos, sect);
			E entry = readEntry(buff);
			pointers.put(getEntryKey(entry), pos);
			idx++;
			//if (xdoc.getTxFinish() == 0) {
			//	actCount++;
			//}
		}
		//buff.position(nextBit()); 
		buff.mark();
		//logger.trace("readDocuments.exit; active docs: {}; documents: {}", actCount, documents);
		return actCount;
	}
	
	private int loadEntries(IMap<K, E> cache) {
		//logger.trace("loadDocuments.enter; docCount: {}", xddCache.size());
		int actCount = 0;
		int sect = 0;
		MappedByteBuffer buff = null;
		//synchronized (buff) {
			for (E entry: cache.values()) {
				int pos = buff.position();
				pos = toPointer(pos, sect);
				pointers.put(getEntryKey(entry), pos);
				writeEntry(buff, entry);
				//if (xdoc.getTxFinish() == 0) {
				//	actCount++;
				//}
			}
			buff.putInt(0, pointers.size());
			buff.mark();
		//}
		//logger.trace("loadDocuments.exit; active docs: {}; documents: {}", actCount, documents);
		return actCount;
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

		fb = buffers.get(buffers.size() - 1);
		synchronized (fb) {
			fb.buff.reset();
			int pos = fb.buff.position();
			int point = toPointer(pos, fb.getSection());
			pointers.put(key, point);
			int cnt = fb.buff.getInt(0);
			cnt++;
			fb.buff.putInt(0, cnt);
			writeEntry(fb.buff, entry);
			fb.buff.mark();
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

	private String getString(MappedByteBuffer buff) {
		int len = buff.getInt();
		byte[] str = new byte[len];
		buff.get(str);
		return new String(str);
	}

	private void putString(MappedByteBuffer buff, String val) {
		byte[] str = val.getBytes();
		buff.putInt(str.length);
		buff.put(str);
	}
    
    
    private class FileBuffer {

    	private int section;
        private FileChannel fc;
    	private RandomAccessFile raf;
        private MappedByteBuffer buff;
        
        FileBuffer(FileChannel fc, RandomAccessFile raf, MappedByteBuffer buff) {
        	this.fc = fc;
        	this.raf = raf;
        	this.buff = buff;
        	// get section from filename
        }
        
        void close() throws IOException {
        	
   			//buff.compact();
   			fc.close();
   			raf.close();
        }
        
        int getSection() {
        	return section;
        }
    	
    }

}
