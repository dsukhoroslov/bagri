package com.bagri.core.server.api.impl;

import static javax.xml.xquery.XQItemType.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;
import com.bagri.core.model.Element;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ModelManagement; 

/**
 * A common implementation part for any future parser. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class ContentParserBase {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected ModelManagement model;
	
	/**
	 * 
	 * @param model the model management component. Used to search/add model paths.
	 */
	protected ContentParserBase(ModelManagement model) {
		this.model = model;
	}
	
	/**
	 * initializes parser components before parsing document
	 */
	protected ParserContext initContext() {
		return new ParserContext();
	}
	
	
	protected class ParserContext {
		
		private int docType = -1;
		private TreeNode<Data> tree;
		private TreeNode<Data> top;
		
		public void addDocument(String root) throws BagriException {
			//String root = "/";
			docType = model.translateDocumentType(root);
			Data data = new Data(root);
			Path path = model.translatePath(docType, "/", NodeKind.document, XQBASETYPE_ANYTYPE, Occurrence.onlyOne);
			Element start = new Element();
			data.setData(path, start);
			tree = new TreeNode<>(data);
			top = tree;
		}

		public Data addData(String name) {
			Data data = new Data(name);
			top.addChild(data);
			return data;
		}

		public Data addData(String name, NodeKind kind, Object value, int dataType, Occurrence occurence) throws BagriException {
			Data data = addData(name);
			setData(kind, value, dataType, occurence);
			return data;
		}
		
		public void addElement() throws BagriException {
			TreeNode<Data> node = getLastNamedNode();
			if (node == null) {
				// element in array
				addData("/");
				node = top.getLastNode();
			}
			Occurrence occurrence = isInArray() ? Occurrence.zeroOrMany : Occurrence.zeroOrOne;
			setData(NodeKind.element, null, XQBASETYPE_ANYTYPE, occurrence);
			top = node;
		}
		
		public void addArray() throws BagriException {
			TreeNode<Data> node = getLastNamedNode();
			if (node == null) {
				// array in document
				addData("/"); // ??
				node = top.getLastNode();
			}
			setData(NodeKind.array, null, XQBASETYPE_ANYTYPE, Occurrence.zeroOrOne);
			top = node;
		}
	
		public void addValue() throws BagriException {
			// set special null value..
			setData(null, XQBASETYPE_ANYATOMICTYPE);
		}

		public void addValue(boolean value) throws BagriException {
			setData(Boolean.valueOf(value), XQBASETYPE_BOOLEAN);
		}

		public void addValue(BigDecimal value) throws BagriException {
			setData(value, XQBASETYPE_DECIMAL);
		}

		public void addValue(long value) throws BagriException {
			setData(Long.valueOf(value), XQBASETYPE_INT);
		}
	
		public void addValue(String value) throws BagriException {
			setData(value, XQBASETYPE_STRING);
		}
		
		public void endElement() throws BagriException {
			Data current = top.getData();
			if (current != null && current.getElement() != null && current.getParentPathId() > 0) {
				Path parent = model.getPath(current.getParentPathId());
				if (parent != null && parent.getPostId() < current.getPostId()) {
					parent.setPostId(current.getPostId());
					model.updatePath(parent);
				}
			}
			top = top.getParent();
		}
		
		public List<Data> getDataList() {
			List<Data> list = new ArrayList<>();
			if (tree != null) {
				tree.fillData(list);
			}
			return list;
		}
		
		public int getDocType() {
			return docType;
		}
		
		public Data getTopData() {
			if (top == null) {
				return null;
			}
			return top.getData();
		}
		
		protected TreeNode<Data> getLastNamedNode() {
			TreeNode<Data> node = top.getLastNode();
			if (node == null) {
				return null;
			}
			Data last = node.getData();
			if (last.getDataPath() == null) {
				return node;
			}
			return null;
		}

		protected boolean isInArray() {
			return top.getData().getNodeKind() == NodeKind.array;
		}

		protected void setData(Object value, int dataType) throws BagriException {
			NodeKind kind;
			Occurrence occurrence;
			if (isInArray()) {
				kind = NodeKind.text;
				occurrence = Occurrence.zeroOrMany;
				TreeNode<Data> node = getLastNamedNode();
				if (node == null) {
					// text in array
					addData("/");
				}
			} else {			
				kind = NodeKind.attribute;
				occurrence = Occurrence.zeroOrOne;
			}
			setData(kind, value, dataType, occurrence);
		}
		
		protected void setData(NodeKind kind, Object value, int dataType, Occurrence occurrence) throws BagriException {
			TreeNode<Data> node = top.getLastNode();
			Data current = node.getData();
			setData(current, top.getData(), kind, value, dataType, occurrence);
		}
		
		protected void setData(Data current, Data parent, NodeKind kind, Object value, int dataType, Occurrence occurrence) throws BagriException {
			logger.trace("setData.enter; current: {}; kind: {}; value: {}; parent: {}", current, kind, value, parent);
			String path;
			if (parent.getNodeKind() == NodeKind.document) {
				path = "/";
			} else {
				path = parent.getPath();
				if (!path.endsWith("/")) {
					path += "/";
				}
			}
			path += current.getDataName();
			Path xPath = model.translatePath(docType, path, kind, dataType, occurrence);
			xPath.setParentId(parent.getPathId());
			if (parent.getPostId() < xPath.getPathId()) {
				Path pPath = parent.getDataPath();
				pPath.setPostId(xPath.getPathId());
				model.updatePath(pPath);
			}
			model.updatePath(xPath);
			int[] position = parent.getPosition();
			position = Arrays.copyOf(position, position.length + 1);
			position[position.length - 1] = parent.addLastChild();
			Element xElt = new Element(position, value); //getAtomicValue(xPath.getDataType(), value));
			current.setData(xPath, xElt);
			logger.trace("setData.exit; updated data: {}", current);
		}
		
		//public void setDocType(int docType) {
		//	this.docType = docType;
		//}
		
	}
	
	
}


	/**
	 * 
	 * @param parent parent data element
	 * @param kind a kind of creating element
	 * @param name creating element name
	 * @param value creating element value. Can be null for non leaf elements 
	 * @param dataType the value data type as per XQJ type constants
	 * @param occurence creating element cardinality 
	 * @return the created XDM data element
	 * @throws BagriException in case of any failure at path translation step
	 */
	//protected Data addData(ParserContext ctx, Data parent, NodeKind kind, String name, String value, int dataType, Occurrence occurence) throws BagriException {
	//	logger.trace("addData.enter; name: {}; kind: {}; value: {}; parent: {}", name, kind, value, parent);
	//	String path = parent.getPath() + name;
	//	Path xPath = model.translatePath(ctx.getDocType(), path, kind, dataType, occurence);
	//	xPath.setParentId(parent.getPathId());
	//	if (parent.getPostId() < xPath.getPathId()) {
	//		Path pPath = parent.getDataPath();
	//		pPath.setPostId(xPath.getPathId());
	//		model.updatePath(pPath);
	//	}
	//	model.updatePath(xPath);
	//	int[] position = parent.getPosition();
		//if (kind == NodeKind.element) {
	//		position = Arrays.copyOf(position, position.length + 1);
	//		position[position.length - 1] = parent.addLastChild();
		//}
	//	Element xElt = new Element(position, getAtomicValue(xPath.getDataType(), value));
	//	Data xData = new Data(xPath, xElt);
	//	ctx.addData(xData);
	//	logger.trace("addData.exit; returning: {}", xData);
	//	return xData;
	//}
	
	/**
	 * 
	 * @param current the current closing element
	 */
	//protected void adjustParent(Data current) {
	//	if (current != null && current.getElement() != null && current.getParentPathId() > 0) {
	//		Path parent = model.getPath(current.getParentPathId());
	//		if (parent != null && parent.getPostId() < current.getPostId()) {
	//			parent.setPostId(current.getPostId());
	//			model.updatePath(parent);
	//		}
	//	}
	//}
	
  

/*

protected class ParserContext {

	protected List<Data> dataList;
	protected LinkedList<Data> dataStack;
	protected int docType = -1;
	
	protected ParserContext() {
		dataList = new ArrayList<Data>();
		dataStack = new LinkedList<Data>(); 
		docType = -1;
	}
	
	public void addData(Data xData) {
		dataList.add(xData);
	}
	
	public void addStack(Data xData) {
		dataStack.push(xData);
	}

	public Data lastData() {
		return dataStack.peekLast(); 
	}
	
	public Data peekData() {
		return dataStack.peek();
	}

	public Data popData() {
		return dataStack.pop();
	}
	
	public List<Data> getDataList() {
		return dataList;
	}
	
	public int getDocType() {
		return docType;
	}
	
	public int getStackSize() {
		return dataStack.size();
	}
	
	public Data getStackElement(int idx) {
		return dataStack.get(idx);
	}
	
	public void setDocType(int docType) {
		this.docType = docType;
	}
	
	//public Iterator<Data> tail() {
	//	return dataStack.descendingIterator();
	//}
}

*/ 

