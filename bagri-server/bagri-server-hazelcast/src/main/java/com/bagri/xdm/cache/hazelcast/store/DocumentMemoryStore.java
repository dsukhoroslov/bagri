package com.bagri.xdm.cache.hazelcast.store;

import static com.bagri.xdm.common.XDMDocumentKey.*;
import static com.bagri.common.util.FileUtils.getStoreFileMask;
import static com.bagri.common.util.FileUtils.buildSectionFileName;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Date;

import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.core.IMap;

public class DocumentMemoryStore extends MemoryMappedStore<Long, XDMDocument> {

	public DocumentMemoryStore(String dataPath, String nodeNum, int buffSize) {
		super(dataPath, nodeNum, buffSize);
	}
	
	@Override
	public void init(IMap<Long, XDMDocument> cache) {

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
				int count = fb.buff.getInt(0);
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
	
	@Override
	protected Long getEntryKey(XDMDocument entry) {
		return entry.getDocumentKey();
	}

	@Override
	protected int getEntrySize(XDMDocument entry) {
		return 4*8 // 4 long's 
			+ 4 // 1 int
			+ entry.getUri().getBytes().length + 4 // uri size
			+ entry.getCreatedBy().getBytes().length + 4 // createdBy size
			+ entry.getEncoding().getBytes().length + 4; // encoding size
	}
	
	@Override
	protected boolean isEntryActive(XDMDocument entry) {
		return entry.getTxFinish() == XDMTransactionManagement.TX_NO;
	}

	@Override
	protected XDMDocument readEntry(MappedByteBuffer buff) {
		long txFinish = buff.getLong();
		long docKey = buff.getLong();
		String uri = getString(buff);
		int typeId = buff.getInt();
		long txStart = buff.getLong();
		Date createdAt = new Date(buff.getLong());
		String createdBy = getString(buff);
		String encoding = getString(buff);
		long documentId = toDocumentId(docKey);
		int version = toVersion(docKey);
		XDMDocument result = new XDMDocument(documentId, version, uri, typeId, txStart, txFinish, createdAt, createdBy, encoding);
		result.setCollections(getIntArray(buff));
		return result;
	}

	@Override
	protected void writeEntry(MappedByteBuffer buff, XDMDocument entry) {
		//if (buff.remaining() < getEntrySize(entry)) {
			//logger.info("writeDocument; remaining: {}, capacity: {}, limit: {}", buff.remaining(), buff.capacity(), buff.limit());
			//buff.
		//}
		buff.putLong(entry.getTxFinish());
		buff.putLong(entry.getDocumentKey());
		putString(buff, entry.getUri());
		buff.putInt(entry.getTypeId());
		buff.putLong(entry.getTxStart());
		buff.putLong(entry.getCreatedAt().getTime());
		putString(buff, entry.getCreatedBy());
		putString(buff, entry.getEncoding());
		putIntArray(buff, entry.getCollections());
	}

	@Override
	protected void deactivateEntry(MappedByteBuffer buff, XDMDocument entry) {
		if (entry.getTxFinish() > XDMTransactionManagement.TX_NO) {
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

