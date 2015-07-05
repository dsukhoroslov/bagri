package com.bagri.xqj;

import static com.bagri.xqj.BagriXQConstants.*;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQMetaData;

import com.xqj2.XQMetaData2;

public class BagriXQMetaData implements XQMetaData, XQMetaData2 {
	
	public static final int max_expression_length = Integer.MAX_VALUE;
	public static final int max_user_name_length = 64;
	
	private Set<String> encodings;
	private String userName;
	private BagriXQConnection connect;
	
	private BagriXQMetaData(String userName) {
		this.userName = userName;
	}

	BagriXQMetaData(BagriXQConnection connect, String userName) {
		this(userName);
		this.connect = connect;
	}
	
	@Override
	public int getProductMajorVersion() throws XQException {
		
		connect.checkConnection();
		return 0;
	}

	@Override
	public int getProductMinorVersion() throws XQException {
		
		connect.checkConnection();
		return 5;
	}

	@Override
	public String getProductName() throws XQException {
		
		connect.checkConnection();
		return "bagri-xqj";
	}

	@Override
	public String getUserName() throws XQException {
		
		connect.checkConnection();
		return userName;
	}

	@Override
	public int getMaxExpressionLength() throws XQException {
		
		connect.checkConnection();
		return max_expression_length;
	}

	@Override
	public int getMaxUserNameLength() throws XQException {
		
		connect.checkConnection();
		return max_user_name_length;
	}

	@Override
	public String getProductVersion() throws XQException {
		
		connect.checkConnection();
		return "0.5.1";
	}

	@Override
	public int getXQJMajorVersion() throws XQException {
		
		connect.checkConnection();
		return 1;
	}

	@Override
	public int getXQJMinorVersion() throws XQException {
		
		connect.checkConnection();
		return 0;
	}

	@Override
	public String getXQJVersion() throws XQException {
		
		connect.checkConnection();
		return "1.0";
	}

	@Override
	public int getXQJ2MajorVersion() throws XQException {

		connect.checkConnection();
		return 1;
	}

	@Override
	public int getXQJ2MinorVersion() throws XQException {
		
		connect.checkConnection();
		return 0;
	}

	@Override
	public String getXQJ2Version() throws XQException {
		
		connect.checkConnection();
		return "1.0";
	}

	@Override
	public boolean isReadOnly() throws XQException {
		
		connect.checkConnection();
		return !connect.getProcessor().isFeatureSupported(xqf_Update);
	}

	@Override
	public boolean isXQueryUpdateFacilitySupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_XQuery_Update_Facility);
	}

	@Override
	public boolean isXQueryFullTextSupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_XQuery_Full_Text);
	}

	@Override
	public boolean isXQuery30Supported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_XQuery_30);
	}

	@Override
	public boolean isXQueryXSupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_XQueryX);
	}

	@Override
	public boolean isTransactionSupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_Transaction);
	}

	@Override
	public boolean isSchemaImportFeatureSupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_Schema_Import);
	}

	@Override
	public boolean isSchemaValidationFeatureSupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_Schema_Validation);
	}

	@Override
	public boolean isFullAxisFeatureSupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_Full_Axis);
	}

	@Override
	public boolean isModuleFeatureSupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_Module);
	}

	@Override
	public boolean isSerializationFeatureSupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_Serialization);
	}

	@Override
	public boolean isStaticTypingFeatureSupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_Static_Typing);
	}

	@Override
	public boolean isStaticTypingExtensionsSupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_Static_Typing_Extensions);
	}

	@Override
	public boolean isXQueryEncodingDeclSupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_XQuery_Encoding_Decl);
	}

	@Override
	public Set<String> getSupportedXQueryEncodings() throws XQException {
		
		connect.checkConnection();
		if (encodings == null) {
			// TODO: client and server side encodings can be different!
			Map<String, Charset> supported = Charset.availableCharsets();
			encodings = supported.keySet();
		}
		return encodings;
	}

	@Override
	public boolean isXQueryEncodingSupported(String encoding) throws XQException {
		
		if (encodings == null) {
			getSupportedXQueryEncodings();
		} else {
			connect.checkConnection();
		}
		return encodings.contains(encoding);
	}

	@Override
	public boolean isUserDefinedXMLSchemaTypeSupported() throws XQException {
		
		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_User_Defined_XML_Schema_Type);
	}

	@Override
	public boolean isXASupported() throws XQException {

		connect.checkConnection();
		return connect.getProcessor().isFeatureSupported(xqf_XA);
	}

	@Override
	public boolean wasCreatedFromJDBCConnection() throws XQException {
		
		connect.checkConnection();
		return false;
	}

}
