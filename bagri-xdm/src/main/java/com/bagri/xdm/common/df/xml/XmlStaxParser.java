package com.bagri.xdm.common.df.xml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.ContentParser;
import com.bagri.xdm.cache.api.ModelManagement;
import com.bagri.xdm.common.df.ContentParserBase;
import com.bagri.xdm.domain.Occurrence;
import com.bagri.xdm.domain.Data;
import com.bagri.xdm.domain.Element;
import com.bagri.xdm.domain.NodeKind;
import com.bagri.xdm.domain.Path;

/**
 * XDM Parser implementation for XML data format. Uses reference implementation (Xerces) of XML streaming parser.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XmlStaxParser extends ContentParserBase implements ContentParser {

	private static XMLInputFactory factory = XMLInputFactory.newInstance();

	private StringBuilder chars;
	private List<XMLEvent> firstEvents;

	/**
	 * 
	 * @param model the model management component
	 * @param xml the document content in XML format
	 * @return the list of parsed XDM data elements
	 * @throws XMLStreamException in case of content read exception
	 * @throws XDMException in case of content parse exception
	 */
	public static List<Data> parseDocument(ModelManagement model, String xml) throws XMLStreamException, XDMException {
		XmlStaxParser parser = new XmlStaxParser(model);
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
	@Override
	public List<Data> parse(String xml) throws XDMException {
		try (Reader reader = new StringReader(xml)) {
			return parse(reader);
		} catch (IOException ex) {
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(File file) throws XDMException {
		try (Reader reader = new FileReader(file)) {
			return parse(reader);
		} catch (IOException ex) {
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(InputStream stream) throws XDMException {
		
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
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(Reader reader) throws XDMException {
		
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
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}
	
	/**
	 * 
	 * @param source the XML source
	 * @return the list of parsed XDM data elements
	 * @throws XDMException in case of content parse exception
	 */
	public List<Data> parse(Source source) throws XDMException {
		
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
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}

	/**
	 * 
	 * @param eventReader the XML streaming parser
	 * @return the list of parsed XDM data elements
	 * @throws XDMException in case of content parse exception
	 */
	public List<Data> parse(XMLEventReader eventReader) throws XDMException {
		
		init();
		while (eventReader.hasNext()) {
			try {
				processEvent(eventReader.nextEvent());
			} catch (XMLStreamException ex) {
				throw new XDMException(ex, XDMException.ecInOut);
			}
		}
		cleanup();

		List<Data> result = dataList;
		dataList = null;
		return result;
	}
	
	private void processEvent(XMLEvent xmlEvent) throws XDMException {
		
		if (docType < 0) {
			firstEvents.add(xmlEvent);
			if (xmlEvent.getEventType() == XMLStreamConstants.START_ELEMENT) {
				String root = "/" + xmlEvent.asStartElement().getName();
				docType = model.translateDocumentType(root);
				for (XMLEvent event: firstEvents) {
					processEvent(event);
				}
			}
		} else {
			switch (xmlEvent.getEventType()) {
				case XMLStreamConstants.START_DOCUMENT:
					processDocument((StartDocument) xmlEvent);
					break;
				case XMLStreamConstants.START_ELEMENT:
					processStartElement(xmlEvent.asStartElement());
					break;
				case XMLStreamConstants.CHARACTERS:
					processCharacters(xmlEvent.asCharacters());
					break;
				case XMLStreamConstants.END_ELEMENT:
					processEndElement(xmlEvent.asEndElement());
					break;
				case XMLStreamConstants.END_DOCUMENT:
					break;
				case XMLStreamConstants.ATTRIBUTE:
					processAttribute((Attribute) xmlEvent);
					break;
				case XMLStreamConstants.COMMENT:
					processComment((Comment) xmlEvent);
					break;
				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					processPI((ProcessingInstruction) xmlEvent);
					break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void cleanup() {
		super.cleanup();
		chars = null;
		firstEvents = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void init() {
		super.init();
		firstEvents = new ArrayList<XMLEvent>();
		chars = new StringBuilder();
	}
	
	private void processDocument(StartDocument document) throws XDMException {

		Element start = new Element();
		start.setElementId(elementId++);
		Path path = model.translatePath(docType, "", NodeKind.document, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne);
		Data data = new Data(path, start);
		dataStack.add(data);
		dataList.add(data);
	}

	@SuppressWarnings("unchecked")
	private void processStartElement(StartElement element) throws XDMException {
		
		Data parent = dataStack.peek();
		Data current = addData(parent, NodeKind.element, "/" + element.getName(), null, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.zeroOrOne); 
		dataStack.add(current);

		for (Iterator<Namespace> itr = element.getNamespaces(); itr.hasNext();) {
			Namespace ns = itr.next();
			// TODO: process default namespace properly
			String nspace = ns.getValue();
			if (nspace != null) {
				String prefix = model.translateNamespace(nspace, ns.getName().getLocalPart());
				addData(current, NodeKind.namespace, "/#" + prefix, nspace, XQItemType.XQBASETYPE_QNAME, Occurrence.onlyOne);
			}
		}

		for (Iterator<Attribute> itr = element.getAttributes(); itr.hasNext();) {
			Attribute a = itr.next();
			// TODO: process additional (not registered yet) namespaces properly
			addData(current, NodeKind.attribute, "/@" + a.getName(), a.getValue(), XQItemType.XQBASETYPE_ANYATOMICTYPE, Occurrence.onlyOne); 
		}
	}

	private void processEndElement(EndElement element) throws XDMException {

		Data current = dataStack.pop();
		if (chars.length() > 0) {
			String content = chars.toString();
			// normalize xml content.. what if it is already normalized??
			content = content.replaceAll("&", "&amp;");
			// trim left/right ? this is schema-dependent. trim if schema-type 
			// is xs:token, for instance..
			Data text = addData(current, NodeKind.text, "/text()", content, XQItemType.XQBASETYPE_ANYATOMICTYPE, Occurrence.zeroOrOne); 
			chars.delete(0, chars.length());
		}
	}

	private void processCharacters(Characters characters) {

		if (characters.getData().trim().length() > 0) {
			chars.append(characters.getData());
		}
	}

	private void processComment(Comment comment) throws XDMException {

		addData(dataStack.peek(), NodeKind.comment, "/comment()", comment.getText(), XQItemType.XQBASETYPE_ANYTYPE, Occurrence.zeroOrOne);
	}

	private void processAttribute(Attribute attribute) {
		// ...
		logger.trace("attribute: {}", attribute);
	}

	private void processPI(ProcessingInstruction pi) throws XDMException {

		//For a processing-instruction node: processing-instruction(local)[position] where local is the name 
		//of the processing instruction node and position is an integer representing the position of the selected 
		//node among its like-named processing-instruction node siblings
		
		Data piData = addData(dataStack.peek(), NodeKind.pi, "/?" + pi.getTarget(), pi.getData(), XQItemType.XQBASETYPE_ANYTYPE, Occurrence.zeroOrOne);
	}

}
