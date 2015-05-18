package com.bagri.xquery.saxon;

import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.api.XDMRepository;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMSchema;

import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.trans.XPathException;
import static com.bagri.xqj.BagriXQConstants.pn_baseURI;

public class ModuleURIResolverImpl implements ModuleURIResolver {
	
	/**
	 * need it because ModuleURIResolver extends Serializable
	 */
	private static final long serialVersionUID = 7617313804143553770L;

	private static final Logger logger = LoggerFactory.getLogger(ModuleURIResolverImpl.class);
	
    private XDMRepository repo;

    public ModuleURIResolverImpl(XDMRepository repo) {
    	this.repo = repo;
		logger.trace("<init>; initialized with Repo: {}", repo);
    }
	
	@Override
	public StreamSource[] resolve(String moduleURI, String baseURI,	String[] locations) throws XPathException {
		logger.trace("resolve.enter; got module: {}, base: {}, locations: {}", moduleURI, baseURI, locations);
		XDMSchema schema = repo.getSchema();
		String moduleName;
		if (locations.length > 0) {
			// locations contains something to use..
			Path path = Paths.get(URI.create(locations[0])); 
			moduleName = path.getFileName().toString();
		} else {
			// moduleURI is the module namespace!
			moduleName = moduleURI;
		}
		XDMModule module = null; //schema.getModule(moduleName);
		if (module != null) {
			Reader mReader = new StringReader(module.getBody());
			return new StreamSource[] {new StreamSource(mReader)};
		} else {
			// throw ex?
			logger.warn("resolve.exit; no module found for name: {}", moduleName);
		}
		return new StreamSource[0];
	}

}
