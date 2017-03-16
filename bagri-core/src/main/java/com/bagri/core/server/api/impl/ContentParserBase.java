package com.bagri.core.server.api.impl;

import static com.bagri.support.util.XQUtils.getAtomicValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

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
	protected Data addData(ParserContext ctx, Data parent, NodeKind kind, String name, String value, int dataType, Occurrence occurence) throws BagriException {
		logger.trace("addData.enter; name: {}; kind: {}; value: {}; parent: {}", name, kind, value, parent);
		String path = parent.getPath() + name;
		Path xPath = model.translatePath(ctx.getDocType(), path, kind, dataType, occurence);
		xPath.setParentId(parent.getPathId());
		if (parent.getPostId() < xPath.getPathId()) {
			Path pPath = parent.getDataPath();
			pPath.setPostId(xPath.getPathId());
			model.updatePath(pPath);
		}
		model.updatePath(xPath);
		int[] position = parent.getPosition();
		//if (kind == NodeKind.element) {
			position = Arrays.copyOf(position, position.length + 1);
			position[position.length - 1] = parent.addLastChild();
		//}
		Element xElt = new Element(position, getAtomicValue(xPath.getDataType(), value));
		Data xData = new Data(xPath, xElt);
		ctx.addData(xData);
		logger.trace("addData.exit; returning: {}", xData);
		return xData;
	}
	
	/**
	 * 
	 * @param current the current closing element
	 */
	protected void adjustParent(Data current) {
		if (current != null && current.getElement() != null && current.getParentPathId() > 0) {
			Path parent = model.getPath(current.getParentPathId());
			if (parent != null && parent.getPostId() < current.getPostId()) {
				parent.setPostId(current.getPostId());
				model.updatePath(parent);
			}
		}
	}
	
	/**
	 * initializes parser components before parsing document
	 */
	protected ParserContext initContext() {
		return new ParserContext();
	}
	
	
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
	
	
}

