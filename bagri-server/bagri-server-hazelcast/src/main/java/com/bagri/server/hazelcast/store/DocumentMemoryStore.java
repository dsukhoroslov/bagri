package com.bagri.server.hazelcast.store;

import static com.bagri.core.api.TransactionManagement.TX_NO;
import static com.bagri.support.util.FileUtils.buildSectionFileName;
import static com.bagri.support.util.FileUtils.getStoreFileMask;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Date;

import com.bagri.core.model.Document;
import com.hazelcast.core.IMap;

public class DocumentMemoryStore extends MemoryMappedStore<Long, Document> {
	
	private long maxTxId = TX_NO;

	public DocumentMemoryStore(String dataPath, String nodeNum, int buffSize) {
		super(dataPath, nodeNum, buffSize);
	}
	
	@Override
	public void init(IMap<Long, Document> cache) {

	    int docCount = 0;
	    int actCount = 0;
		boolean found = false;
		Path root = Paths.get(dataPath);
		String mask = getStoreFileMask(nodeNum, "catalog");
		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(mask);
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(Path path) throws IOException {
				return matcher.matches(path.getFileName());
			}
		};

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, filter)) {
		    for (Path path: stream) {
	    		//logger.trace("processPathFiles; path: {}; uri: {}", path.toString(), path.toUri().toString());
	        	String fileName = path.toString();
	        	int section = getSection(path.getFileName().toString()); 
	        	FileBuffer fb = initBuffer(fileName, section);
				int count = fb.getCount();
				logger.info("init; buffer {} initialized, going to read {} documents from file: {}", section, count, fileName);
				actCount += readEntries(fb);
				docCount += count;
	        	found = true;
		    }
		} catch (IOException ex) {
			logger.error("init; error reading catalog:", ex);
		}
		
		if (!found) {
			int section = 0;
			String fileName = getBufferName(section);
			try {
				initBuffer(fileName, section);
			} catch (IOException ex) {
				logger.error("init; error initializing catalog:", ex);
			}
		}
		
		// now synch it with cache anyway, regardless of found files or not
		if (!found) {
			logger.info("init; no existing catalog files found, going to load documents from cache");
			docCount = cache.size();
			actCount = loadEntries(cache);
		}
		logger.info("init; active documents: {}, out of total: {}", actCount, docCount);
		initialized = true;
	}
	
	public boolean isActivated() {
		return initialized;
	}
	
	public int getActiveEntryCount() {
		return cntActive.get();
	}
	
	public int getFullEntryCount() {
		return pointers.size();
	}
	
	public long getMaxTransactionId() {
		return maxTxId;
	}
	
	@Override
	protected Long getEntryKey(Document entry) {
		return entry.getDocumentKey();
	}

	@Override
	protected int getEntrySize(Document entry) {
		return 4*8 // 4 long's (txStart, txFinish, docKey, createdAt)
			+ 2*4 // 2 int's (bytes, elements)
			+ entry.getUri().getBytes().length + 4 // uri size
			+ entry.getTypeRoot().getBytes().length + 4 // root size
			+ entry.getCreatedBy().getBytes().length + 4 // createdBy size
			+ entry.getEncoding().getBytes().length + 4 // encoding size
			+ entry.getCollections().length*4 + 4; // collections
	}
	
	@Override
	protected boolean isEntryActive(Document entry) {
		return entry.getTxFinish() == TX_NO;
	}

	@Override
	protected Document readEntry(MappedByteBuffer buff) {
		long txFinish = buff.getLong();
		long docKey = buff.getLong();
		String uri = getString(buff);
		String root = getString(buff);
		long txStart = buff.getLong();
		Date createdAt = new Date(buff.getLong());
		String createdBy = getString(buff);
		String encoding = getString(buff);
		int bytes = buff.getInt();
		int elts = buff.getInt();
		Document result = new Document(docKey, uri, root, txStart, txFinish, createdAt, createdBy, encoding, bytes, elts);
		result.setCollections(getIntArray(buff));
		if (txFinish > maxTxId) {
			maxTxId = txFinish;
		} else if (txStart > maxTxId) {
			maxTxId = txStart;
		}
		return result;
	}

	@Override
	protected void writeEntry(MappedByteBuffer buff, Document entry) {
		//if (buff.remaining() < getEntrySize(entry)) {
			//logger.info("writeDocument; remaining: {}, capacity: {}, limit: {}", buff.remaining(), buff.capacity(), buff.limit());
			//buff.
		//}
		buff.putLong(entry.getTxFinish());
		buff.putLong(entry.getDocumentKey());
		putString(buff, entry.getUri());
		putString(buff, entry.getTypeRoot());
		buff.putLong(entry.getTxStart());
		buff.putLong(entry.getCreatedAt().getTime());
		putString(buff, entry.getCreatedBy());
		putString(buff, entry.getEncoding());
		buff.putInt(entry.getBytes());
		buff.putInt(entry.getElements());
		putIntArray(buff, entry.getCollections());
	}

	@Override
	protected void deactivateEntry(MappedByteBuffer buff, Document entry) {
		if (entry == null) {
			logger.info("deactivateEntry; got null entry for some reason!"); 
		} else if (entry.getTxFinish() > TX_NO) {
			buff.putLong(entry.getTxFinish());
		} else {
			buff.putLong(-1); // make some const for this..
		}
	}
	
	@Override
	protected String getBufferName(int section) {
		return buildSectionFileName(dataPath, nodeNum, "catalog", section);
	}

}

