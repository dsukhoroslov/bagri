package com.bagri.xquery.saxon;

import java.util.Properties;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import com.bagri.xquery.api.XQCompiler;

public class XQCompilerImpl implements XQCompiler {
	
	private Properties props = new Properties();
	
    private Configuration config;
    private StaticQueryContext sqc;
	
    public XQCompilerImpl() {
        config = Configuration.newConfiguration();
        config.setHostLanguage(Configuration.XQUERY);
        config.setSchemaValidationMode(Validation.STRIP);
        //FunctionLibraryList list;
        //list.
        //config.setConfigurationProperty(FeatureKeys.PRE_EVALUATE_DOC_FUNCTION, Boolean.TRUE);
        sqc = config.newStaticQueryContext();
    }

	@Override
	public Properties getProperties() {
		return props;
	}

	@Override
	public void setProperty(String name, Object value) {
		props.setProperty(name, value.toString());
	}

	@Override
	public void compileQuery(String query) {
		try {
			XQueryExpression exp = sqc.compileQuery(query);
		} catch (XPathException ex) {
			throw new RuntimeException(ex);
		}
	}

}
