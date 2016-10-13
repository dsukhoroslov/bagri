package com.bagri.xquery.saxon;

import static com.bagri.xquery.saxon.SaxonUtils.*; 

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.instruct.UserFunctionParameter;
import net.sf.saxon.functions.ExecutableFunctionLibrary;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.Annotation;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.query.XQueryFunction;
import net.sf.saxon.query.XQueryFunctionLibrary;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.system.DataType;
import com.bagri.xdm.system.Function;
import com.bagri.xdm.system.Library;
import com.bagri.xdm.system.Module;
import com.bagri.xdm.system.Parameter;
import com.bagri.xdm.system.XQueryTrigger;
import com.bagri.xquery.api.XQCompiler;
import com.bagri.xquery.saxon.extension.GetDocument;
import com.bagri.xquery.saxon.extension.LogOutput;
import com.bagri.xquery.saxon.extension.RemoveCollectionDocuments;
import com.bagri.xquery.saxon.extension.RemoveDocument;
import com.bagri.xquery.saxon.extension.StaticFunctionExtension;
import com.bagri.xquery.saxon.extension.StoreDocument;

public class XQCompilerImpl implements XQCompiler {
	
	private static final Logger logger = LoggerFactory.getLogger(XQCompilerImpl.class);
	
	private Properties props = new Properties();
	
    private Configuration config;
	private List<Library> libraries = new ArrayList<>();
	
    public XQCompilerImpl() {
    	initializeConfig();
    }

	@Override
	public Properties getProperties() {
		return props;
	}

	@Override
	public void setProperty(String name, Object value) {
		props.setProperty(name, value.toString());
	}
	
	private String getError(XPathException ex, StaticQueryContext sqc) {
		StringBuffer buff = new StringBuffer();
		if (sqc.getErrorListener() instanceof LocalErrorListener) {
			List<TransformerException> errors = ((LocalErrorListener) sqc.getErrorListener()).getErrors();
			for (TransformerException tex: errors) {
				buff.append(tex.getMessageAndLocation()).append("\n");
			}
		} else {
			Throwable err = ex;
			while (err != null) {
				buff.append(err.getMessage()).append("\n");
				err = err.getCause();
			}
		}
		return buff.toString();
	}

	@Override
	public void compileQuery(String query) throws XDMException {
		long stamp = System.currentTimeMillis();
		logger.trace("compileQuery.enter; got query: {}", query);
		StaticQueryContext sqc = null;
		try {
		    sqc = prepareStaticContext(null);
			sqc.compileQuery(query);
		} catch (XPathException ex) {
			String error = getError(ex, sqc);
			logger.info("compileQuery.error; message: {}", error);
			throw new XDMException(error, XDMException.ecQueryCompile);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("compileQuery.exit; time taken: {}", stamp); 
	}

	@Override
	public void compileModule(Module module) throws XDMException {
		long stamp = System.currentTimeMillis();
		logger.trace("compileModule.enter; got module: {}", module);
		getModuleExpression(module);
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("compileModule.exit; time taken: {}", stamp); 
	}

	@Override
	public String compileTrigger(Module module, XQueryTrigger trigger) throws XDMException {
		long stamp = System.currentTimeMillis();
		logger.trace("compileTrigger.enter; got trigger: {}", trigger);
		String query = "import module namespace " + module.getPrefix() + 
				"=\"" + module.getNamespace() + 
				"\" at \"" + module.getName() + "\";\n" +
				"declare variable $doc external;\n\n" +
				trigger.getFunction() + "($doc)\n"; 
	    StaticQueryContext sqc = prepareStaticContext(module.getBody());
		logger.trace("getModuleExpression; compiling query: {}", query);
		try {
			sqc.compileQuery(query);
		} catch (XPathException ex) {
			String error = getError(ex, sqc);
			//logger.error("compileQuery.error", ex);
			logger.info("compileTrigger.error; message: {}", error);
			throw new XDMException(error, XDMException.ecQueryCompile);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("compileTrigger.exit; time taken: {}", stamp);
		return query;
	}
	
	@Override
	public List<String> getModuleFunctions(Module module) throws XDMException {
		long stamp = System.currentTimeMillis();
		logger.trace("getModuleFunctions.enter; got module: {}", module);
		XQueryExpression exp = getModuleExpression(module);
		List<String> result = lookupFunctions(exp.getExecutable().getFunctionLibrary(), new FunctionExtractor<String>() {

			@Override
			public String extractFunction(XQueryFunction fn) {
				String decl = getFunctionDeclaration(fn.getUserFunction()); 
				try {
					Field f = fn.getClass().getDeclaredField("annotationMap"); 
					f.setAccessible(true);
					Map<StructuredQName, Annotation> atns = (HashMap<StructuredQName, Annotation>) f.get(fn); 
					logger.trace("lookupFunctions; fn annotations: {}", atns);
					StringBuffer buff = new StringBuffer();
					for (Annotation atn: atns.values()) {
						buff.append(atn.getAnnotationQName().getDisplayName());
						if (atn.getAnnotationParameters() != null) {
							buff.append("(");
							int cnt = 0;
							for (AtomicValue av: atn.getAnnotationParameters()) {
								if (cnt > 0) {
									buff.append(", ");
								}
								buff.append("\"").append(av.getStringValue()).append("\"");
								cnt++;
							}
							buff.append(")");
						}
						buff.append("\n");
					}
					decl = buff.toString() + decl;
				} catch (NoSuchFieldException | IllegalAccessException ex) {
					logger.warn("lookupFunctions. error accessing annotations: {}", ex);
				} catch (Exception ex) {
					logger.error("lookupFunctions.error: {}", ex);
				}
				return decl;
			}
			
		});
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("getModuleFunctions.exit; time taken: {}; returning: {}", stamp, result);
		return result;
	}

	private String getFunctionDeclaration(UserFunction function) {
		//declare function hw:helloworld($name as xs:string)
		logger.trace("getFunctionDeclaration.enter; function: {}", function);
		StringBuffer buff = new StringBuffer("function ");
		buff.append(function.getFunctionName());
		buff.append("(");
		int idx =0;
		for (UserFunctionParameter ufp: function.getParameterDefinitions()) {
			if (idx > 0) {
				buff.append(", ");
			}
			buff.append("$");
			buff.append(ufp.getVariableQName());
			buff.append(" as ");
			buff.append(ufp.getRequiredType().toString());
			idx++;
		}
		buff.append(") as ");
		// TODO: get rid of Q{} notation..
		buff.append(function.getDeclaredResultType().toString());
		String result = buff.toString();
		logger.trace("getFunctionDeclaration.exit; returning: {}", result);
		return result;
	}

	
	@Override
	public boolean getModuleState(Module module) {
		try {
			String query = "import module namespace test=\"" + module.getNamespace() + 
					"\" at \"" + module.getName() + "\";\n\n";
			query += "1213";
		    StaticQueryContext sqc = prepareStaticContext(module.getBody());
			logger.trace("getModuleExpression; compiling query: {}", query);
			sqc.compileQuery(query);
			return true;
		} catch (XPathException ex) {
			return false;
		}
	}
	
	@Override
	public void setLibraries(Collection<Library> libraries) {
		this.libraries.clear();
		this.libraries.addAll(libraries);
		//config.registerExtensionFunction(function);
		initializeConfig();
	}
	
	private void initializeConfig() {
		
		logger.trace("initializeConfig.enter; current config: {}", config);
        config = Configuration.newConfiguration();
        //config.setHostLanguage(Configuration.XQUERY);
        config.setSchemaValidationMode(Validation.STRIP);
        //config.setConfigurationProperty(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS, Boolean.TRUE);

        config.registerExtensionFunction(new LogOutput());
        config.registerExtensionFunction(new GetDocument(null));
        config.registerExtensionFunction(new RemoveDocument(null));
        config.registerExtensionFunction(new StoreDocument(null));
        config.registerExtensionFunction(new RemoveCollectionDocuments(null));
        
        if (libraries != null) {
        	registerExtensions(config, libraries);
        }
		logger.trace("initializeConfig.exit; new config: {}", config);
	}
	
	static void registerExtensions(Configuration config, Collection<Library> libraries) {
		for (Library lib: libraries) {
			for (Function func: lib.getFunctions()) {
				try {
					ExtensionFunctionDefinition efd = new StaticFunctionExtension(func, config);
					logger.trace("registerExtensions; funtion {} registered as {}", func.toString(), efd.getFunctionQName()); 
					config.registerExtensionFunction(efd);
				} catch (Exception ex) {
					logger.warn("registerExtensions; error registering function {}: {}; skipped", func.toString(), ex.getMessage());
				}
			}
		}
	}
	
	private StaticQueryContext prepareStaticContext(String body) {
		StaticQueryContext sqc = config.newStaticQueryContext();
		sqc.setErrorListener(new LocalErrorListener());
        //sqc.setCompileWithTracing(true);
		if (body != null) {
			sqc.setModuleURIResolver(new LocalModuleURIResolver(body));
		}
		return sqc;
	}
	
	private XQueryExpression getModuleExpression(Module module) throws XDMException {
		//logger.trace("getModuleExpression.enter; got namespace: {}, name: {}, body: {}", namespace, name, body);
		String query = "import module namespace test=\"" + module.getNamespace() + 
				"\" at \"" + module.getName() + "\";\n\n";
		query += "1213";
		StaticQueryContext sqc = null;
		try {
			//sqc.compileLibrary(query); - works in EE only
		    sqc = prepareStaticContext(module.getBody());
			logger.trace("getModuleExpression; compiling query: {}", query);
			//logger.trace("getModuleExpression.exit; time taken: {}", stamp);
			return sqc.compileQuery(query);
			//sqc.getCompiledLibrary("test")...
		} catch (XPathException ex) {
			logger.error("getModuleExpression.error; {}", ex);
			String error = getError(ex, sqc);
			//logger.info("getModuleExpression.error; message: {}", error);
			throw new XDMException(error, XDMException.ecQueryCompile);
		}
	}

	private <R> List<R> lookupFunctions(FunctionLibraryList fll, FunctionExtractor<R> ext) {
		List<R> fl = new ArrayList<>();
		for (FunctionLibrary lib: fll.getLibraryList()) {
			logger.trace("lookupFunctions; function library: {}; class: {}", lib.toString(), lib.getClass().getName());
			if (lib instanceof FunctionLibraryList) {
				fl.addAll(lookupFunctions((FunctionLibraryList) lib, ext));
			//} else if (lib instanceof ExecutableFunctionLibrary) {
			//	ExecutableFunctionLibrary efl = (ExecutableFunctionLibrary) lib;
			//	Iterator<UserFunction> itr = efl.iterateFunctions();
			//	while (itr.hasNext()) {
			//		fl.add(getFunctionDeclaration(itr.next()));
			//	}
			} else if (lib instanceof XQueryFunctionLibrary) {
				XQueryFunctionLibrary xqfl = (XQueryFunctionLibrary) lib;
				Iterator<XQueryFunction> itr = xqfl.getFunctionDefinitions();
				while (itr.hasNext()) {
					XQueryFunction fn = itr.next();
					logger.trace("lookupFunctions; fn: {}", fn.getDisplayName());
					R result = ext.extractFunction(fn);
					if (result != null) {
						fl.add(result);
					}
				}
			}
		}
		return fl;
	}
	
	@Override
    public List<Function> getRestFunctions(Module module) throws XDMException {
		long stamp = System.currentTimeMillis();
		logger.trace("getRestFunctions.enter; got module: {}", module);
		XQueryExpression exp = getModuleExpression(module);
		List<Function> result = lookupFunctions(exp.getExecutable().getFunctionLibrary(), new FunctionExtractor<Function>() {

			@Override
			public Function extractFunction(XQueryFunction fn) {
				logger.trace("extractFunction.enter; function: {}", fn);
				Map<StructuredQName, Annotation> atns = null;
				try {
					Field f = fn.getClass().getDeclaredField("annotationMap"); 
					f.setAccessible(true);
					atns = (HashMap<StructuredQName, Annotation>) f.get(fn); 
					logger.trace("extractFunction; fn annotations: {}", atns);
				} catch (NoSuchFieldException | IllegalAccessException ex) {
					logger.warn("extractFunction. error accessing annotations: {}", ex);
				} catch (Exception ex) {
					logger.error("extractFunction.error: {}", ex);
				}
				if (atns == null) {
					return null;
				}
				if (!hasRestAnnotations(atns.keySet())) {
					logger.debug("extractFunction; no REST annotations found for function {}, skipping it", fn.getFunctionName().getDisplayName());
					return null;
				}
				
				DataType type = new DataType(getTypeName(fn.getResultType().getPrimaryType()), getCardinality(fn.getResultType().getCardinality()));
				Function result = new Function(null, fn.getFunctionName().getLocalPart(), type, null, fn.getFunctionName().getPrefix());
				for (UserFunctionParameter ufp: fn.getParameterDefinitions()) {
					Parameter param = new Parameter(ufp.getVariableQName().getLocalPart(), getTypeName(ufp.getRequiredType().getPrimaryType()), 
							getCardinality(ufp.getRequiredType().getCardinality()));
					result.getParameters().add(param);
				}

				for (Annotation atn: atns.values()) {
					String aName = atn.getAnnotationQName().getDisplayName();
					if (aName.startsWith("rest:")) {
						result.addAnnotation(aName, null);
						if (atn.getAnnotationParameters() != null) {
							for (AtomicValue av: atn.getAnnotationParameters()) {
								result.addAnnotation(aName, av.getStringValue());
							}								
						}
					}
				}
				
				logger.trace("extractFunction.exit; returning: {}", result);
				return result;
			}
			
		});
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("getRestFunctions.exit; time taken: {}; returning: {}", stamp, result);
		return result;
    }
	
	private boolean hasRestAnnotations(Set<StructuredQName> annotations) {
		for (StructuredQName sn: annotations) {
			if ("rest".equalsIgnoreCase(sn.getPrefix())) {
				return true;
			}
		}
		return false;
	}
	
	private interface FunctionExtractor<R> {
		
		R extractFunction(XQueryFunction fn);
		
	}
	
	private class LocalErrorListener implements ErrorListener {

	    private List<TransformerException> errors = new ArrayList<>();
	    
	    public List<TransformerException> getErrors() {
	    	return errors;
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
	}
	
	private class LocalModuleURIResolver implements ModuleURIResolver {
		
		private String body;
		
		LocalModuleURIResolver(String body) {
			this.body = body;
		}

		@Override
		public StreamSource[] resolve(String moduleURI, String baseURI,	String[] locations) throws XPathException {
			logger.trace("resolve.enter; got moduleURI: {}, baseURI: {}, locations: {}, body: {}", 
					moduleURI, baseURI, locations, body);
			Reader reader = new StringReader(body);
			return new StreamSource[] {new StreamSource(reader)};
		}
	}

}
