package com.bagri.samples.client;

public interface BagriClientApp {
	
	void close() throws Exception;
	boolean createDocument(String uri, String content) throws Exception;
	String readDocument(String uri) throws Exception;
	boolean updateDocument(String uri, String content) throws Exception;
	void deleteDocument(String uri) throws Exception;
	String queryDocumentByUri(String uri) throws Exception;
	String queryDocumentFromCollection() throws Exception;
	
	interface Tester {
		void testClient(BagriClientApp client) throws Exception;
	}
	
	static final Tester tester = new Tester() { 
	
		public void testClient(BagriClientApp client) throws Exception {
			
			String uri = "test_document";
			try {
				String xml = "<content>XML Content</content>";
				if (!client.createDocument(uri, xml)) {
					System.out.println("ERROR: document was not created");
					//return;
				}
				xml = "<content>Updated XML Content</content>";
				if (!client.updateDocument(uri, xml)) {
					System.out.println("ERROR: document was not updated");
					//return;
				}
				xml = client.readDocument(uri);
				if (xml != null) {
					System.out.println("got document: " + xml);
				} else {
					System.out.println("ERROR: document was not read");
					//return;
				}
				xml = client.queryDocumentFromCollection();
				if (xml != null) {
					System.out.println("got document from collection: " + xml);
				} else {
					System.out.println("ERROR: document was not queried from collection");
					//return;
				}
				xml = client.queryDocumentByUri(uri);
				if (xml != null) {
					System.out.println("got document by uri: " + xml);
				} else {
					System.out.println("ERROR: document was not queried by uri");
					//return;
				}
				client.deleteDocument(uri);
				xml = client.readDocument(uri);
				if (xml != null) {
					System.out.println("ERROR: document still exists: " + xml);
				}
			} finally {
				client.close();
			}
		}
	};

}
