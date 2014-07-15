package com.bagri.xdm.access.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.idgen.SimpleIdGenerator;
//import com.bagri.cache.api.PathDictionary;
import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;

public class XDMStaxParser {

	private static final Logger logger = LoggerFactory.getLogger(XDMStaxParser.class);

	private static XMLInputFactory factory = XMLInputFactory.newInstance();
	private StringBuilder chars;
	private List<XDMElement> dataList;

	//private long elementId = 0;
	private long documentId = 0;
	private Stack<XDMElement> dataStack;
	private IdGenerator<Long> idGen;
	private XDMSchemaDictionary dict;

	public static List<XDMElement> parseDocument(XDMSchemaDictionary dictionary, long documentId, 
			IdGenerator generator, String xml) throws IOException, XMLStreamException {
		XDMStaxParser parser;
		if (generator == null) {
			parser = new XDMStaxParser(dictionary, documentId);
		} else {
			parser = new XDMStaxParser(dictionary, documentId, generator);
		}
		return parser.parse(xml);
	}
	
	//public XDMStaxParser() {
	//	this(0);
	//}
	
	public XDMStaxParser(XDMSchemaDictionary dict, long documentId) {
		this.dict = dict;
		this.documentId = documentId;
		this.idGen = new SimpleIdGenerator(0);
	}

	public XDMStaxParser(XDMSchemaDictionary dict, long documentId, IdGenerator generator) {
		this.dict = dict;
		this.documentId = documentId;
		this.idGen = generator;
	}

	public List<XDMElement> parse(String xml) throws IOException, XMLStreamException {
		//FileInputStream in = new FileInputStream(file);
		Reader reader = new StringReader(xml);
		return parse(reader);
	}
	
	public List<XDMElement> parse(File file) throws IOException, XMLStreamException {
		FileInputStream stream = new FileInputStream(file);
		//Reader reader = new FileReader(file);
		return parse(stream);
	}
	
	public List<XDMElement> parse(InputStream stream) throws IOException, XMLStreamException {
		
		return parse(factory.createXMLEventReader(stream));
	}
	
	public List<XDMElement> parse(Reader reader) throws IOException, XMLStreamException {
		
		return parse(factory.createXMLEventReader(reader));
	}
	
	public List<XDMElement> parse(Source source) throws IOException, XMLStreamException {
		
		return parse(factory.createXMLEventReader(source));
	}
	
	public List<XDMElement> parse(XMLEventReader eventReader) throws IOException, XMLStreamException {
		
		while (eventReader.hasNext()) {

			XMLEvent xmlEvent = eventReader.nextEvent();
			//logger.trace("event: {}", xmlEvent);

			switch (xmlEvent.getEventType()) {

				case XMLStreamConstants.START_DOCUMENT:
					init();
					break;
				case XMLStreamConstants.START_ELEMENT:
					processStartElement(xmlEvent.asStartElement());
					//eventReader.nextTag();
					break;
				case XMLStreamConstants.CHARACTERS:
					processCharacters(xmlEvent.asCharacters());
					break;
				case XMLStreamConstants.END_ELEMENT:
					processEndElement(xmlEvent.asEndElement());
					//eventReader.nextTag();
					break;
				case XMLStreamConstants.END_DOCUMENT:
					cleanup();
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

		List<XDMElement> result = dataList;
		dataList = null;
		return result;
	}

	private void cleanup() {
		chars = null;
	}

	private void init() {
		dataList = new ArrayList<XDMElement>();
		dataStack = new Stack<XDMElement>();

		XDMElement start = new XDMElement();
		start.setElementId(idGen.next());
		start.setDocumentId(documentId);
		start.setKind(XDMNodeKind.document);
		start.setParentId(0);
		start.setPath("");
		dataStack.add(start);
		dataList.add(start);

		chars = new StringBuilder();
	}

	private void processStartElement(StartElement element) {

		XDMElement parent = dataStack.peek();
		XDMElement current = addData(parent, XDMNodeKind.element, null); 
		current.setName(element.getName().getLocalPart());
		current.setPath(parent.getPath() + "/" + element.getName());
		dataStack.add(current);

		for (Iterator<Namespace> itr = element.getNamespaces(); itr.hasNext();) {
			Namespace ns = itr.next();
			XDMElement nspace = addData(current, XDMNodeKind.namespace, ns.getValue()); 
			String name = ns.getName().getLocalPart();
			nspace.setName(name);
			//nspace.setPath(null); 
			if (name != null && name.trim().length() > 0) {
				name = "xmlns:" + name;
			} else {
				name = "xmlns";
			}
			nspace.setPath(current.getPath() + "/#" + name);
		}

		for (Iterator<Attribute> itr = element.getAttributes(); itr.hasNext();) {
			Attribute a = itr.next();
			XDMElement attr = addData(current, XDMNodeKind.attribute, a.getValue());
			attr.setName(a.getName().getLocalPart());
			attr.setPath(current.getPath() + "/@" + a.getName());
		}
	}

	private void processEndElement(EndElement element) {

		XDMElement current = dataStack.pop();
		//logger.trace("current: {}", current);
		//logger.trace("end chars: {}", chars.toString());
		//logger.trace("element: {}", element);
		if (chars.length() > 0) {
			String content = chars.toString();
			// normalize xml content.. what if it is already normalized??
			content = content.replaceAll("&", "&amp;");
			XDMElement text = addData(current, XDMNodeKind.text, content); 
			chars.delete(0, chars.length());
			//logger.trace("text: {}", text);
		}
	}

	private void processCharacters(Characters characters) {

		//logger.trace("characters: {}", characters);
		//logger.trace("chars: {}", characters.getData());
		if (characters.getData().trim().length() > 0) {
			chars.append(characters.getData());
		}
	}

	private void processComment(Comment value) {

		XDMElement comment = addData(XDMNodeKind.comment, value.getText());
	}

	private void processAttribute(Attribute attribute) {
		// ...
		//logger.trace("attribute: {}", attribute);
	}

	private void processPI(ProcessingInstruction value) {

		XDMElement pi = addData(XDMNodeKind.pi, value.getData());
		pi.setName(value.getTarget());
	}

	private XDMElement addData(XDMNodeKind kind, String value) {
		
		return addData(dataStack.peek(), kind, value);
	}

	private XDMElement addData(XDMElement top, XDMNodeKind kind, String value) {
		
		XDMElement data = new XDMElement();
		data.setElementId(idGen.next());
		data.setDocumentId(documentId);
		data.setParentId(top.getElementId());
		String path = top.getPath();
		if (kind == XDMNodeKind.text) {
			path += "/text()";
		} 
		data.setPath(path);
		data.setKind(kind);
		data.setValue(value);
		dataList.add(data);
		return data;
	}
	
}