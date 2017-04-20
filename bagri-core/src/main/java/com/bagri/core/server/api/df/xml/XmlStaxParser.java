package com.bagri.core.server.api.df.xml;

import static javax.xml.stream.XMLInputFactory.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.xquery.XQItemType;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;
import com.bagri.core.model.Element;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Null;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentParserBase;

/**
 * XDM Parser implementation for XML data format. Uses reference implementation (Xerces) of XML streaming parser.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XmlStaxParser extends ContentParserBase implements ContentParser {

	private static XMLInputFactory factory = XMLInputFactory.newInstance();

	/**
	 * 
	 * @param model the model management component
	 * @param xml the document content in XML format
	 * @return the list of parsed XDM data elements
	 * @throws XMLStreamException in case of content read exception
	 * @throws BagriException in case of content parse exception
	 */
	public static List<Data> parseDocument(ModelManagement model, String xml) throws XMLStreamException, BagriException {
		XmlStaxParser parser = new XmlStaxParser(model);
		parser.init(new Properties());
		return parser.parse(xml);
	}
	
	/**
	 * 
	 * @param model the model management component
	 */
	public XmlStaxParser(ModelManagement model) {
		super(model);
	}

    /**
     * {@inheritDoc}
     */
 	public void init(Properties properties) {
 		logger.trace("init; got context: {}", properties);
		for (Map.Entry prop: properties.entrySet()) {
			String name = (String) prop.getKey();
			if (factory.isPropertySupported(name)) {
				String value = (String) prop.getValue();
				if (value != null && value.length() > 0) {
					if (name.equals(ALLOCATOR) || name.equals(REPORTER) || name.equals(RESOLVER)) {
						factory.setProperty(name, value);
					} else {
						factory.setProperty(name, Boolean.valueOf(value));
					}
				}
			}
		}
 	}
 	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(String xml) throws BagriException {
		try (Reader reader = new StringReader(xml)) {
			return parse(reader);
		} catch (IOException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(File file) throws BagriException {
		try (Reader reader = new FileReader(file)) {
			return parse(reader);
		} catch (IOException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(InputStream stream) throws BagriException {
		
		XMLEventReader eventReader = null;
		try {
			try {
				eventReader = factory.createXMLEventReader(stream); 
				return parse(eventReader);
			} finally {
				if (eventReader != null) {
					eventReader.close();
				}
			}
		} catch (XMLStreamException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(Reader reader) throws BagriException {
		
		XMLEventReader eventReader = null;
		try {
			try {
				eventReader = factory.createXMLEventReader(reader); 
				return parse(eventReader);
			} finally {
				if (eventReader != null) {
					eventReader.close();
				}
			}
		} catch (XMLStreamException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		}
	}
	
	/**
	 * 
	 * @param source the XML source
	 * @return the list of parsed XDM data elements
	 * @throws BagriException in case of content parse exception
	 */
	public List<Data> parse(Source source) throws BagriException {
		
		XMLEventReader eventReader = null;
		try {
			try {
				eventReader = factory.createXMLEventReader(source); 
				return parse(eventReader);
			} finally {
				if (eventReader != null) {
					eventReader.close();
				}
			}
		} catch (XMLStreamException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		}
	}

	/**
	 * 
	 * @param eventReader the XML streaming parser
	 * @return the list of parsed XDM data elements
	 * @throws BagriException in case of content parse exception
	 */
	public List<Data> parse(XMLEventReader eventReader) throws BagriException {
		
		XmlParserContext ctx = initContext();
		while (eventReader.hasNext()) {
			try {
				processEvent(ctx, eventReader.nextEvent());
			} catch (XMLStreamException ex) {
				throw new BagriException(ex, BagriException.ecInOut);
			}
		}
		return ctx.getDataList();
	}
	
	private void processEvent(XmlParserContext ctx, XMLEvent xmlEvent) throws BagriException {
		
		if (ctx.getDocRoot() == null) {
			ctx.firstEvents.add(xmlEvent);
			if (xmlEvent.getEventType() == XMLStreamConstants.START_ELEMENT) {
				String root = "/" + xmlEvent.asStartElement().getName();
				ctx.addDocument(root);
				for (XMLEvent event: ctx.firstEvents) {
					processEvent(ctx, event);
				}
			}
		} else {
			switch (xmlEvent.getEventType()) {
				case XMLStreamConstants.START_ELEMENT:
					startElement(ctx, xmlEvent.asStartElement());
					break;
				case XMLStreamConstants.CHARACTERS:
					if (!xmlEvent.asCharacters().isWhiteSpace()) {
						ctx.addCharacters(xmlEvent.asCharacters().getData());
					}
					break;
				case XMLStreamConstants.END_ELEMENT:
					ctx.endElement();
					break;
				case XMLStreamConstants.ATTRIBUTE:
					ctx.addAttribute(((Attribute) xmlEvent).getName(), ((Attribute) xmlEvent).getValue());
					break;
				case XMLStreamConstants.COMMENT:
					ctx.addComment(((Comment) xmlEvent).getText());
					break;
				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					ctx.addProcessingInstruction(((ProcessingInstruction) xmlEvent).getTarget(), ((ProcessingInstruction) xmlEvent).getData());
					break;
				default:
					break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected XmlParserContext initContext() {
		return new XmlParserContext();
	}
	
	@SuppressWarnings("unchecked")
	private void startElement(XmlParserContext ctx, StartElement element) throws BagriException {
		ctx.appendCharacters();
		
		ctx.addData(element.getName().toString()); // getLocalPart());
		ctx.addElement(); 

		for (Iterator<Namespace> itr = element.getNamespaces(); itr.hasNext();) {
			Namespace ns = itr.next();
			// TODO: process default namespace properly
			String namespace = ns.getValue();
			if (namespace != null) {
				String prefix = ns.getName().getLocalPart();
				ctx.addNamespace(prefix, namespace);
			}
		}

		for (Iterator<Attribute> itr = element.getAttributes(); itr.hasNext();) {
			Attribute a = itr.next();
			//if (!a.getName().getPrefix().isEmpty()) {
			//	String prefix = model.translateNamespace(a.getName().getNamespaceURI(), a.getName().getPrefix());
			//	ctx.addNamespace(prefix, a.getName().getNamespaceURI());
			//}
			ctx.addAttribute(a.getName(), a.getValue()); 
		}
	}


	private class XmlParserContext extends ParserContext {

		private StringBuilder chars;
		private Set<String> nspaces;
		private List<XMLEvent> firstEvents;
		
		XmlParserContext() {
			super();
			firstEvents = new ArrayList<XMLEvent>();
			nspaces = new HashSet<>();
			chars = new StringBuilder();
		}
		
		private void addAttribute(QName name, String value) throws BagriException {
			logger.trace("attribute: {}:{}", name, value);
			addData("@" + name, NodeKind.attribute, value, XQItemType.XQBASETYPE_ANYATOMICTYPE, Occurrence.onlyOne); 
		}

		public void addCharacters(String data) {
			chars.append(data);
		}
		
		boolean appendCharacters() throws BagriException {
			if (chars.length() > 0) {
				String content = chars.toString();
				// normalize xml content.. what if it is already normalized??
				content = content.replaceAll("&", "&amp;");
				// trim left/right ? this is schema-dependent. trim if schema-type 
				// is xs:token, for instance..
				addData("text()", NodeKind.text, content, XQItemType.XQBASETYPE_ANYATOMICTYPE, Occurrence.zeroOrOne); 
				chars.delete(0, chars.length());
				return true;
			}
			return false;
		}

		public void addComment(String comment) throws BagriException {
			addData("comment()", NodeKind.comment, comment, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.zeroOrOne);
		}
		
		public void addNamespace(String prefix, String namespace) throws BagriException {
			// "xml" namespace is assumed, no need to add it 
			if (!"xml".equals(prefix) && nspaces.add(prefix)) {
				addData("#" + prefix, NodeKind.namespace, namespace, XQItemType.XQBASETYPE_QNAME, Occurrence.onlyOne);
			}
		}
		
		public void addProcessingInstruction(String target, String data) throws BagriException {
			//For a processing-instruction node: processing-instruction(local)[position] where local is the name 
			//of the processing instruction node and position is an integer representing the position of the selected 
			//node among its like-named processing-instruction node siblings
			addData("/?" + target, NodeKind.pi, data, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.zeroOrOne);
		}
		
		@Override
		public void endElement() throws BagriException {
			if (!appendCharacters()) {
				if (isTopEmpty()) {
					addData("text()", NodeKind.text, Null._null, XQItemType.XQBASETYPE_ANYATOMICTYPE, Occurrence.zeroOrOne);
				}
			}
			super.endElement();
		}

	}
}