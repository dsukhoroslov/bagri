package com.bagri.xqj;

import static com.bagri.xqj.BagriXQConstants.ex_connection_closed;

import java.util.Set;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQMetaData;

import com.xqj2.XQMetaData2;

public class BagriXQMetaData implements XQMetaData, XQMetaData2 {
	
	private String userName;
	private XQConnection connect;
	
	BagriXQMetaData(String userName) {
		this.userName = userName;
	}

	BagriXQMetaData(XQConnection connect, String userName) {
		this(userName);
		this.connect = connect;
	}

	@Override
	public int getXQJ2MajorVersion() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return 1;
	}

	@Override
	public int getXQJ2MinorVersion() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return 0;
	}

	@Override
	public String getXQJ2Version() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return "1.0";
	}

	@Override
	public boolean isXQueryUpdateFacilitySupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return true;
	}

	@Override
	public boolean isXQueryFullTextSupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return true;
	}

	@Override
	public boolean isXQuery30Supported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return true;
	}

	@Override
	public int getProductMajorVersion() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return 0;
	}

	@Override
	public int getProductMinorVersion() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return 5;
	}

	@Override
	public String getProductName() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return "bagri-xqj";
	}

	@Override
	public String getProductVersion() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return "0.5.1";
	}

	@Override
	public int getXQJMajorVersion() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return 1;
	}

	@Override
	public int getXQJMinorVersion() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return 0;
	}

	@Override
	public String getXQJVersion() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return "1.0";
	}

	@Override
	public boolean isReadOnly() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return false;
	}

	@Override
	public boolean isXQueryXSupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return false;
	}

	@Override
	public boolean isTransactionSupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return false;
	}

	@Override
	public boolean isStaticTypingFeatureSupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSchemaImportFeatureSupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSchemaValidationFeatureSupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFullAxisFeatureSupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isModuleFeatureSupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSerializationFeatureSupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isStaticTypingExtensionsSupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getUserName() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return userName;
	}

	@Override
	public int getMaxExpressionLength() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaxUserNameLength() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return 64;
	}

	@Override
	public boolean wasCreatedFromJDBCConnection() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		return false;
	}

	@Override
	public boolean isXQueryEncodingDeclSupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set getSupportedXQueryEncodings() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isXQueryEncodingSupported(String encoding) throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUserDefinedXMLSchemaTypeSupported() throws XQException {
		
		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isXASupported() throws XQException {

		if (connect.isClosed()) {
			throw new XQException(ex_connection_closed);
		}
		
		// TODO Auto-generated method stub
		return false;
	}

}
