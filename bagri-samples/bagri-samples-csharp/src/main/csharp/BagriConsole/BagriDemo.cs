using System;
using com.bagri.core;
using com.bagri.xqj;
using java.util;
using javax.xml.xquery;
using javax.xml.@namespace;

public class BagriDemo
{

    private XQConnection xqConn;

    static void Main(string[] args)
    {

        Console.WriteLine("Expected arguments: connection_string(bagri_host:port) schema_name username password");
        String addr = "";
        Console.Write("connection_string: ");
        if (args.Length > 0) {
            Console.WriteLine(args[0]);
            addr = args[0];
        } else {
            addr = Console.ReadLine();
        }

        String schema = "";
        Console.Write("schema_name: ");
        if (args.Length > 1) {
            Console.WriteLine(args[1]);
            schema = args[1];
        } else {
            schema = Console.ReadLine();
        }

        String user = "";
        Console.Write("username: ");
        if (args.Length > 2) {
            Console.WriteLine(args[2]);
            user = args[2];
        } else {
            user = Console.ReadLine();
        }

        String pwd = "";
        Console.Write("password: ");
        if (args.Length > 3) {
            Console.WriteLine(args[3]);
            pwd = args[3];
        } else {
            pwd = Console.ReadLine();
        }

        Properties props = new Properties();
        props.setProperty(BagriXQDataSource.ADDRESS, addr);
        props.setProperty(BagriXQDataSource.SCHEMA, schema);
        props.setProperty(BagriXQDataSource.USER, user);
        props.setProperty(BagriXQDataSource.PASSWORD, pwd);
        props.setProperty(BagriXQDataSource.XQ_PROCESSOR, "com.bagri.xquery.saxon.XQProcessorClient");
        props.setProperty(BagriXQDataSource.XDM_REPOSITORY, "com.bagri.client.hazelcast.impl.SchemaRepositoryImpl");

        Console.WriteLine("Connecting to Bagri...");
        BagriDemo demo = new BagriDemo(props);
        Console.WriteLine("Connection established: " + demo.xqConn.ToString());

        String uri = UUID.randomUUID().toString();
        try {
            String xml = "<content>XML Content</content>";
            if (!demo.createDocument(uri, xml)) {
                Console.WriteLine("ERROR: document was not created");
            } else {
                Console.WriteLine("document was created with content: " + xml);
            }

            xml = "<content>Updated XML Content</content>";
            if (!demo.updateDocument(uri, xml)) {
                Console.WriteLine("ERROR: document was not updated");
            } else {
                Console.WriteLine("document content was updated to: " + xml);
            }

            xml = demo.readDocument(uri);
            if (xml != null) {
                Console.WriteLine("got document: " + xml);
            } else {
                Console.WriteLine("ERROR: document was not read");
            }

            xml = demo.queryDocumentFromCollection();
            if (xml != null) {
                Console.WriteLine("got document from collection: " + xml);
            } else {
                Console.WriteLine("ERROR: document was not found in default collection");
            }

            xml = demo.queryDocumentByUri(uri);
            if (xml != null) {
                Console.WriteLine("got document for uri: " + xml);
            } else {
                Console.WriteLine("ERROR: document was not found for uri: " + uri);
            }

            demo.deleteDocument(uri);
            xml = demo.readDocument(uri);
            if (xml != null) {
                Console.WriteLine("ERROR: document still exists: " + xml);
            } else {
                Console.WriteLine("document was deleted successfully");
            }

        } finally {
            demo.close();
        }
        Console.WriteLine("Connection closed. Now press Enter to close app...");
        Console.ReadLine();
    }

    public BagriDemo(Properties props)
    {
        XQDataSource xqds = new BagriXQDataSource();
        xqds.setProperties(props);
        xqConn = xqds.getConnection();
    }

    public void close()
    {
        xqConn.close();
    }

    public Boolean createDocument(String uri, String content) //throws XQException
    {
        String result = storeDocument(uri, content);
  	    return result != null;
    }

    public String readDocument(String uri) //throws XQException
    {
        String query = "declare namespace bgdb=\"http://bagridb.com/bdb\";\n" +
			"declare variable $uri external;\n" + 
			"let $doc := bgdb:get-document-content($uri)\n" +
			"return $doc\n";

        XQPreparedExpression xqpe = xqConn.prepareExpression(query);
        xqpe.bindString(new QName("uri"), uri, xqConn.createAtomicType(8)); //XQItemType.XQBASETYPE_ANYURI));
        XQResultSequence xqs = xqpe.executeQuery();
        String result = null;
        if (xqs.next()) {
		    result = xqs.getItemAsString(null);
        }
        return result;
    }

    public String queryDocumentByUri(String uri) //throws XQException
    {
        String query = "for $doc in fn:doc(\"" + uri + "\")\n" +
			"return $doc\n";

        XQExpression xqe = xqConn.createExpression();
        XQResultSequence xqs = xqe.executeQuery(query);
        String result = null;
        if (xqs.next()) {
		    result = xqs.getItemAsString(null);
        }
        return result;
    }

    public String queryDocumentFromCollection() //throws XQException
    {
        String query = "for $doc in fn:collection()\n" +
			"return $doc\n";

        XQExpression xqe = xqConn.createExpression();
        XQResultSequence xqs = xqe.executeQuery(query);
        String result = null;
        if (xqs.next()) {
		    result = xqs.getItemAsString(null);
        }
        return result;
    }

    public Boolean updateDocument(String uri, String content) //throws XQException
    {
        String result = storeDocument(uri, content);
  	    return uri.Equals(result);
    }

    private void deleteDocument(String uri) //throws XQException
    {
        String query = "declare namespace bgdb=\"http://bagridb.com/bdb\";\n" +
		    "declare variable $uri external;\n" + 
			"let $uri := bgdb:remove-document($uri)\n" + 
			"return $uri\n";

        XQPreparedExpression xqpe = xqConn.prepareExpression(query);
        xqpe.bindString(new QName("uri"), uri, xqConn.createAtomicType(8)); //XQItemType.XQBASETYPE_ANYURI));
        XQSequence xqs = xqpe.executeQuery();
        String result = null;
        try {
	        if (xqs.next()) {
	    	    result = xqs.getAtomicValue();
	        }
	        if (!uri.Equals(result)) {
	    	    throw new XQException("got no result from bgdb:remove-document function");
	        }
        } finally {
            xqpe.close();
	        xqs.close();
	    }
	}

    private String storeDocument(String uri, String content) //throws XQException
    {
        String query = "declare namespace bgdb=\"http://bagridb.com/bdb\";\n" +
            "declare variable $uri external;\n" +
            "declare variable $xml external;\n" +
            "declare variable $props external;\n" +
            "let $uri := bgdb:store-document($uri, $xml, $props)\n" +
            "return $uri\n";
    
        XQPreparedExpression xqpe = xqConn.prepareExpression(query);
        xqpe.bindString(new QName("uri"), uri, xqConn.createAtomicType(8)); //XQItemType.XQBASETYPE_ANYURI));
        xqpe.bindString(new QName("xml"), content, xqConn.createAtomicType(29)); //XQItemType.XQBASETYPE_STRING));
        List props = new ArrayList(2);
        props.add(Constants.pn_document_data_format + "=xml");
        // 
        xqpe.bindSequence(new QName("props"), xqConn.createSequence(props.iterator()));
        XQSequence xqs = xqpe.executeQuery();
        String result = null;
        try {
            if (xqs.next()) {
                result = xqs.getAtomicValue();
            }
        } finally {
            xqpe.close();
            xqs.close();
        }
        return result;
    }

}
