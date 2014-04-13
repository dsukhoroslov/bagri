/**
 * 
 */
package com.bagri.xquery.saxon;

import java.util.Collections;
import java.util.List;

import javax.xml.transform.SourceLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.om.AttributeCollection;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.pull.PullProvider;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.AttributeCollectionImpl;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.StringValue;

/**
 * @author Denis Sukhoroslov
 *
 */
public class BagriXDMPullProvider implements PullProvider {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriXDMPullProvider.class);
	
    private PipelineConfiguration pipe;
    private int state = START_OF_INPUT;
    private Configuration config;
    
    public BagriXDMPullProvider(Configuration config) {
    	this.config = config;
    }

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#setPipelineConfiguration(net.sf.saxon.event.PipelineConfiguration)
	 */
	@Override
	public void setPipelineConfiguration(PipelineConfiguration pipe) {
		logger.debug("setPipelineConfiguration; pipe: {}", pipe);
		this.pipe = pipe;
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#getPipelineConfiguration()
	 */
	@Override
	public PipelineConfiguration getPipelineConfiguration() {
		logger.debug("getPipelineConfiguration; pipe: {}", pipe);
		return pipe;
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#next()
	 */
	@Override
	public int next() throws XPathException {
		// TODO Auto-generated method stub
		switch (state) {
			case START_OF_INPUT: state = START_DOCUMENT; break; 
			case START_DOCUMENT: state = START_ELEMENT; break;
			case START_ELEMENT: state = TEXT; break;
			case END_ELEMENT: state = END_DOCUMENT; break;
			case END_DOCUMENT: state = END_OF_INPUT; break;
			case END_OF_INPUT: break;
			case ATOMIC_VALUE: break;
			case ATTRIBUTE: state = TEXT; break;
			case COMMENT: break;
			case NAMESPACE: break;
			case PROCESSING_INSTRUCTION: break;
			case TEXT: state = END_ELEMENT; break;
		}
		logger.debug("next; returning: {}", state);
		return state;
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#current()
	 */
	@Override
	public int current() {
		logger.debug("current; returning: {}", state);
		return state;
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#getAttributes()
	 */
	@Override
	public AttributeCollection getAttributes() throws XPathException {
		// TODO Auto-generated method stub
		logger.debug("getAttributes; returning: {}", "empty");
		return new AttributeCollectionImpl(config);
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#getNamespaceDeclarations()
	 */
	@Override
	public NamespaceBinding[] getNamespaceDeclarations() throws XPathException {
		// TODO Auto-generated method stub
		logger.debug("getNamespaceDeclarations; returning: {}", "[]");
		return new NamespaceBinding[0];
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#skipToMatchingEnd()
	 */
	@Override
	public int skipToMatchingEnd() throws XPathException {
		if (state == START_DOCUMENT) {
			state = END_DOCUMENT;
		} else {
			state = END_ELEMENT;
		}
		logger.debug("skipToMatchingEnd; returning: {}", state);
		return state;
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub
		logger.debug("close; state: {}", state);
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#getNameCode()
	 */
	@Override
	public int getNameCode() {
		// TODO Auto-generated method stub
		logger.debug("getNameCode; returning: {}", state);
		return state;
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#getFingerprint()
	 */
	@Override
	public int getFingerprint() {
		// TODO Auto-generated method stub
		logger.debug("getFingerprint; returning: {}", state);
		return state;
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#getStringValue()
	 */
	@Override
	public CharSequence getStringValue() throws XPathException {
		// TODO Auto-generated method stub
		logger.debug("getStringValue; returning: {}", "text");
		return "text";
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#getSchemaType()
	 */
	@Override
	public SchemaType getSchemaType() {
		// TODO Auto-generated method stub
		logger.debug("getSchemaType; returning: {}", "untyped");
		return Untyped.getInstance();
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#getAtomicValue()
	 */
	@Override
	public AtomicValue getAtomicValue() {
		// TODO Auto-generated method stub
		logger.debug("getAtomicValue; returning: {}", "text");
		return new StringValue("text"); //null;
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#getSourceLocator()
	 */
	@Override
	public SourceLocator getSourceLocator() {
		// TODO Auto-generated method stub
		logger.debug("getSourceLocator; returning: {}", "null");
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.pull.PullProvider#getUnparsedEntities()
	 */
	@Override
	public List getUnparsedEntities() {
		// TODO Auto-generated method stub
		logger.debug("getUnparsedEntities; returning: {}", "empty");
		return Collections.EMPTY_LIST;
	}

}
