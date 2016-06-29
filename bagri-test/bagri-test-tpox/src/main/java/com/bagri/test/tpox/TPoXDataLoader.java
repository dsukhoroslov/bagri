package com.bagri.test.tpox;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.client.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.domain.Document;

public class TPoXDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(TPoXDataLoader.class);
	
	private ExecutorService exec;
	private SchemaRepository xRepo;
	
	public static void main(String[] args) {

		String directory = args[0];
		String header = args[1];
		int size = Integer.parseInt(args[2]);
		System.out.println("processing directory: " + directory + "; header: " + header);

		TPoXDataLoader loader = new TPoXDataLoader();
		loader.initialize(size);
		int count;
		try {
			count = loader.process(directory, header);
			System.out.println("processed documents: " + count);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void initialize(int poolSize) {
   		xRepo = new RepositoryImpl();
		exec = Executors.newFixedThreadPool(poolSize);
		logger.trace("initialize; got repository: {}", xRepo);
	}
	
	public int process(String folder, String header) throws NumberFormatException, IOException {

		//String input = "\"<XDS FIL='batch-1.xml' OFF='11762' LEN='4311' />\"";

		InputStream fis = new FileInputStream(folder + "\\" + header);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis)); //, Charset.forName("UTF-8")));
		String line;
		int cnt = 0;
		while ((line = br.readLine()) != null) {
			String[] sp = line.split("'");
			String fileName = folder + "\\" + sp[1];
			int offset = Integer.parseInt(sp[3]);
			int length = Integer.parseInt(sp[5]);
			//processDocument(fileName, offset, length);
			Runnable worker = new StoreThread(fileName, offset, length);
			exec.execute(worker);
			cnt++;
		}
		logger.trace("process; flushed {} documents", cnt);

		exec.shutdown();
		while (!exec.isTerminated()) {
		}
			
		// Done with the file
		br.close();
		fis.close();
		return cnt;
	}
	
	private void processDocument(String fileName, int offset, int length) {

		logger.trace("processDocument.enter; fileName: {}; offset: {}; length: {}",
				new Object[] {fileName, offset, length});
		try {
			FileInputStream fis = new FileInputStream(fileName);
			FileChannel inChannel = fis.getChannel();
			ByteBuffer buffer = ByteBuffer.allocateDirect(length);
			int cnt = inChannel.read(buffer, offset);
			//System.out.println("cnt=" + cnt + "; pos=" + buffer.position());
			buffer.position(0);
			byte[] bytearr = new byte[buffer.remaining()];
		    buffer.get(bytearr);
			String xml = new String(bytearr);
			//System.out.println(xml);
			buffer.clear(); 
			inChannel.close();
			fis.close();
			
			String uri = fileName + "/" + offset; 
			uri = storeDocument(uri, xml);
			logger.trace("processDocument.exit; document stored as {}", uri);
		} catch (Exception ex) {
			logger.error("processDocument; error reading document", ex);
		}
	}

	private String storeDocument(String uri, String xml) {

		try {
			Document doc = xRepo.getDocumentManagement().storeDocumentFromString(uri, xml, null);
			//logger.trace("storeDocument.exit; result: {}", result);
			return doc.getUri();
		} catch (XDMException ex) {
			return null;
		}
	}
	
	
	private class StoreThread implements Runnable {
		
		private String fileName;
		private int offset;
		private int length;
		
		StoreThread(String fileName, int offset, int length) {
			this.fileName = fileName;
			this.offset = offset;
			this.length = length;
		}

		@Override
		public void run() {
			processDocument(fileName, offset, length);
		}
		
	}

}
