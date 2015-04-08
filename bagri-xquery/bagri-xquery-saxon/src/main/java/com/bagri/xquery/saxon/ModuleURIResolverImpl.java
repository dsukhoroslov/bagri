package com.bagri.xquery.saxon;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMRepository;

import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.trans.XPathException;

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
		//repo.getBaseURI ..
		return null;
	}

}
