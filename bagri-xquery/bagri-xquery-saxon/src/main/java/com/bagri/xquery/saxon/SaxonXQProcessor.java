package com.bagri.xquery.saxon;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.ObjectValue;

import com.bagri.xquery.api.XQProcessorBase;

public abstract class SaxonXQProcessor extends XQProcessorBase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected XQDataFactory xqFactory;
    
    
    //@Override
    public void setXQDataFactory(XQDataFactory xqFactory) {
    	this.xqFactory = xqFactory;
    }
    
	//@Override
	public String convertToString(Object item) throws XQException {
		
		if (item instanceof NodeOverNodeInfo) {
			try {
				return QueryResult.serialize(((NodeOverNodeInfo) item).getUnderlyingNodeInfo());
			} catch (XPathException ex) {
				throw new XQException(ex.getMessage());
			}
		} else if (item instanceof Node) {
		    StringWriter sw = new StringWriter();
		    try {
		    	Transformer t = TransformerFactory.newInstance().newTransformer();
		    	t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    	t.setOutputProperty(OutputKeys.INDENT, "yes");
		    	t.transform(new DOMSource((Node) item), new StreamResult(sw));
		    } catch (TransformerException te) {
		    	throw new XQException(te.getMessage());
		    }
		    return sw.toString();
		} else if (item instanceof ObjectValue) {
			return convertToString(((ObjectValue) item).getObject());
		} else if (item instanceof XQSequence) {
			return ((XQSequence) item).getSequenceAsString(null);
		} else if (item instanceof XQItem) {
			return ((XQItem) item).getItemAsString(null);
		} else {
			return item.toString();
		}
	}
	
	//public XQItemType getItemType(Object item) throws XQException {
	//	XQItemType type = null;
	//	if (item instanceof AtomicValue) {
	//		int base = BagriJPConverter.getBaseType((AtomicValue) item);
     //     	type = new BagriXQItemType(base, XQItemType.XQITEMKIND_ATOMIC, null, 
    //      			BagriXQDataFactory.getTypeName(base), false, null);
	//	}
	//	return type;
	//}

	
}
