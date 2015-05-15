package com.bagri.xquery.saxon;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import com.bagri.xquery.api.XQCompiler;

public class XQCompilerImpl implements XQCompiler, ErrorListener {
	
	private static final Logger logger = LoggerFactory.getLogger(XQCompilerImpl.class);
	
	private Properties props = new Properties();
	
    private Configuration config;
    private StaticQueryContext sqc;
    private List<TransformerException> errors = new ArrayList<>();
	
    public XQCompilerImpl() {
        config = Configuration.newConfiguration();
        config.setHostLanguage(Configuration.XQUERY);
        config.setSchemaValidationMode(Validation.STRIP);
        //FunctionLibraryList list;
        //list.
        //config.setConfigurationProperty(FeatureKeys.PRE_EVALUATE_DOC_FUNCTION, Boolean.TRUE);
        sqc = config.newStaticQueryContext();
        sqc.setErrorListener(this);
        //sqc.setCompileWithTracing(true);
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
		long stamp = System.currentTimeMillis();
		logger.trace("compileQuery.enter; got query: {}", query);
		clearErrors();
		try {
			XQueryExpression exp = sqc.compileQuery(query);
		} catch (XPathException ex) {
			StringBuffer buff = new StringBuffer();
			for (TransformerException tex: errors) {
				buff.append(tex.getMessageAndLocation()).append("\n");
			}
			String error = buff.toString();
			//logger.error("compileQuery.error", ex);
			logger.info("compileQuery.error; message: {}", error);
			throw new RuntimeException(error);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("compileQuery.exit; time taken: {}", stamp); 
	}

	@Override
	public void compileModule(String namespace, String name, String body) {
		long stamp = System.currentTimeMillis();
		logger.trace("compileModule.enter; got namespace: {}, name: {}, body: {}", namespace, name, body);
		clearErrors();
		try {
			//sqc.compileLibrary(query); - works in EE only
			String query = "import module namespace test=\"" + namespace + "\" at \"" + name + "\";\n\n";
			query += "1213";
			sqc.setModuleURIResolver(new LocalModuleURIResolver(body));
			logger.trace("compileModule; compiling query: {}", query);
			XQueryExpression exp = sqc.compileQuery(query);
			//exp.
		} catch (XPathException ex) {
			StringBuffer buff = new StringBuffer();
			for (TransformerException tex: errors) {
				buff.append(tex.getMessageAndLocation()).append("\n");
			}
			String error = buff.toString();
			//logger.error("compileQuery.error", ex);
			logger.info("compileModule.error; message: {}", error);
			throw new RuntimeException(error);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("compileModule.exit; time taken: {}", stamp); 
	}
	
	private void clearErrors() {
		errors.clear();
	}

	@Override
	public void error(TransformerException txEx) throws TransformerException {
		errors.add(txEx);
	}

	@Override
	public void fatalError(TransformerException txEx) throws TransformerException {
		errors.add(txEx);
	}

	@Override
	public void warning(TransformerException txEx) throws TransformerException {
		errors.add(txEx);
	}
	
	private class LocalModuleURIResolver implements ModuleURIResolver {
		
		private String body;
		
		LocalModuleURIResolver(String body) {
			this.body = body;
		}

		@Override
		public StreamSource[] resolve(String moduleURI, String baseURI,	String[] locations) throws XPathException {
			Reader reader = new StringReader(body);
			return new StreamSource[] {new StreamSource(reader)};
		}
	}

}
