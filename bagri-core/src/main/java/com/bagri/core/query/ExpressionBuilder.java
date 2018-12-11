package com.bagri.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build expressions of right type from parameters provided.
 * Contains expressions belonging to the same collection only?
 * 
 * @author Denis Sukhoroslov
 *
 */
public class ExpressionBuilder {
	
    private static final transient Logger logger = LoggerFactory.getLogger(ExpressionBuilder.class);

	private int exIndex = -1;
	private List<Expression> expressions = new ArrayList<>();
	
	/**
	 * Add expression to internal expression list
	 * 
	 * @param exp the expression
	 * @return the index at which expression is stored in internal expression list
	 */
	public int addExpression(Expression exp) {
		expressions.add(exp);
		exIndex = expressions.size() - 1;
		return resolveCurrentParent();
	}
	
	/**
	 * Build expression and add it to internal expression list
	 * 
	 * @param clnId the collection identifier
	 * @param compType the comparison type
	 * @param path the expression path
	 * @param param the parameter name
	 * @return the index at which expression is stored in internal expression list
	 */
	public int addExpression(int clnId, Comparison compType, PathBuilder path, String param) {
		Expression ex;
		switch (compType) {
			case AND:
			case OR:
				ex = new BinaryExpression(clnId, compType, path);
				break;
			case NOT:
				return -1;
			default:
				ex = new PathExpression(clnId, compType, path, param);
		}
		return addExpression(ex);
	}
	
	/**
	 * 
	 * @return the last added expression
	 */
	private Expression getCurrentExpression() {
		return getExpression(exIndex);
	}
	
	/**
	 * 
	 * @param exIdx the expression index
	 * @return the expression if it is found by the index provided, null otherwise
	 */
	public Expression getExpression(int exIdx) {
		if (exIdx < 0) {
			return null;
		}
		if (exIdx >= expressions.size()) {
			return null;
		}
		return expressions.get(exIdx);
	}
	
	/**
	 * 
	 * @return the list of containing expressions 
	 */
	public List<Expression> getExpressions() {
		return Collections.unmodifiableList(expressions);
	}
	
	/**
	 * 
	 * @return collectionId associated with this builder
	 */
	public int getCollectionId() {
		if (expressions.size() > 0) {
			return expressions.get(0).getCollectionId();
		}
		return 0;
	}

	/**
	 * 
	 * @return root expression
	 */
	public Expression getRoot() {
		if (expressions.size() > 0) {
			return expressions.get(0);
		}
		return null;
	}
	
	private int resolveCurrentParent() {
		Expression current = getCurrentExpression();
		for (int i=exIndex - 1; i >= 0; i--) {
			Expression ex = expressions.get(i);
			if (ex instanceof BinaryExpression) {
				BinaryExpression be = (BinaryExpression) ex;
				if (be.getLeft() == null) {
					be.setLeft(current);
					return i;
				} else if (be.getRight() == null) {
					be.setRight(current);
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder buff = new StringBuilder();
		buff.append("ExpressionBuilder; size: ").append(expressions.size());
		if (expressions.size() > 0) {
			buff.append(" [");
			buff.append(expressions.get(0));
			buff.append("]");
		}
		return buff.toString();
	}
	
	
	public boolean buildExpression(String query, int clnId) {
		Parser p = new Parser(this, clnId);
		if (p.parse(query)) {
			return expressions.size() > 0;
		}
		return false;
	}
	
	private class Parser {
		
		private int brCount = 0;
		private int collectId;
		private Frame root;
		private ArrayList<Token> tokens = new ArrayList<>();
		
		Parser(ExpressionBuilder builder, int collectId) {
			this.collectId  = collectId;
		}
	
		boolean parse(String query) {
			Lexer lx = new Lexer(query);
			String tkn;
			while ((tkn = lx.nextToken()) != null) {
				Token token = new Token(tkn);
				logger.trace("parse; token: {}", token);
				switch (token.getType()) {
					case open: handleOpen(); break;
					case close: handleClose(); break;
					case comp: handleComparison(token); break;
					case path: handlePath(token); break;
					case param: handleParam(token); break;
					case scan: handleFullScan(token); break;
					default: 
					  // unexpected token..
					  return false;
				}
			}
			
			if (brCount == 0) {
				if (root == null) {
					if (tokens.size() == 1) {
						if (tokens.get(0).type == TokenType.scan) {
							addExpression(new AlwaysExpression(collectId));
						}
					} else if (tokens.size() == 3) {
						Token param = tokens.get(tokens.size() - 1);
						Token comp = tokens.get(tokens.size() - 2);
						Token path = tokens.get(tokens.size() - 3);
						if (path.getType() == TokenType.path && comp.getType() == TokenType.comp && param.getType() == TokenType.param) {
							Expression ex = new PathExpression(collectId, comp.getComparison(), new PathBuilder(path.getToken()), param.getToken());
							buildExpressions(ex);
							return true;
						}
					}
				} else {
					buildExpressions(root.top);
					return true;
				}
			}
			return false;
		}

		private void handleOpen() {
			brCount++;
			// check state..
			Token last = getLastToken();
			if (last == null) {
				Frame frame = new Frame();
				frame.top = new BinaryExpression(collectId, Comparison.AND);
				if (root == null) {
					root = frame;
				} else {
					Frame end = getLastFrame();
					end.next = frame;
					frame.parent = end;
				}
			} else if (last.getType() == TokenType.comp) {
				Comparison comp = last.getComparison();
				if (Comparison.isBinary(comp)) {
					Frame end = getLastFrame();
					end.top.setCompType(comp);
				} // what else?
			}
			tokens.clear();
		}
		
		private void handleClose() {
			brCount--;
			Token last = getLastToken();
			if (last == null) {
				Frame end = getLastFrame();
				end.parent.next = null;
				((BinaryExpression) end.parent.top).setRight(end.top);
			} else if (last.getType() == TokenType.param) {
				Token comp = tokens.get(tokens.size() - 2);
				if (comp.getType() == TokenType.comp) {
					Token path = tokens.get(tokens.size() - 3);
					if (path.getType() == TokenType.path || path.getType() == TokenType.param) {
						Frame end = getLastFrame();
						Expression ex = new PathExpression(collectId, comp.getComparison(), new PathBuilder(path.getToken()), last.getToken());
						if (((BinaryExpression) end.top).getLeft() == null) {
							((BinaryExpression) end.top).setLeft(ex);
						} else {
							((BinaryExpression) end.top).setRight(ex);
						}
					}
				}
			}
			tokens.clear();
		}
		
		private void handleFullScan(Token token) {
			tokens.clear();
			tokens.add(token);
		}
	
		private void handleComparison(Token token) {
			if (token.getComparison() == null) {
				// throw ex?
			}
			tokens.add(token);
		}
	
		private void handlePath(Token token) {
			//
			tokens.add(token);
		}

		private void handleParam(Token token) {
			//
			tokens.add(token);
		}
		
		private Frame getLastFrame() {
			Frame last = root;
			while (last.next != null) {
				last = last.next;
			}
			return last;
		}
		
		private Token getLastToken() {
			if (tokens.size() > 0) {
				return tokens.get(tokens.size() - 1);
			}
			return null;
		}
		
		private void buildExpressions(Expression top) {
			if (top instanceof BinaryExpression) {
				BinaryExpression be = (BinaryExpression) top;
				if (be.isComplete()) {
					addExpression(top);
					buildExpressions(be.getLeft());
					buildExpressions(be.getRight());
				} else {
					// expect only left set
					buildExpressions(be.getLeft());
				}
			} else {
				addExpression(top);
			}
		}
		
	}
	
	private static class Frame {
		
		private Expression top;

		private Frame next;
		private Frame parent;
	}
	
	private static class Lexer {
		
		private String input;
		private int pos;
		private String token;
		
		Lexer(String input) {
			this.input = input;
			this.pos = 0;
			this.token = "";
 		}
		
		String nextToken() {
			if (pos >= input.length()) {
				if (token.length() > 0) {
					String result = token;
					token = "";
					return result;
				}
				return null;
			}
			char c = input.charAt(pos);
			pos++;
			switch (c) {
				case '(': if (token.length() > 0) {
					String result = token;
					token = "";
					return result;
				}
				return "(";
				case ')': if (token.length() > 0) {
					String result = token;
					token = "";
					pos--;
					return result;
				}
				return ")";
				case ' ': if (token.length() > 0) {
					String result = token;
					token = "";
					return result;
				}
			}
			if (c != ' ') {
				token += c;
			}
			return nextToken();
		}
		
	}
	
	private enum TokenType {
		
		open, // (
		close, // )
		comp, // =, !=, <, <=, >, >=, in, like, between, and, or, not... 
		path, // starts with /
		param,
		scan; // full-scan
	}
	
	private static class Token {
		
		private String token;
		private TokenType type;
		
		Token(String token) {
			this.token = token;
			this.type = getType();
		}
		
		String getToken() {
			return token;
		}
		
		private TokenType getType() {
			if ("(".equals(token)) {
				return TokenType.open;
			}
			if (")".equals(token)) {
				return TokenType.close;
			}
			if ("full-scan".equals(token)) {
				return TokenType.scan;
			}
			if (token.startsWith("/")) {
				return TokenType.path;
			}
			if ("=".equals(token) || "!=".equals(token) || "<".equals(token) || "<=".equals(token) || ">=".equals(token) || ">".equals(token) ||
					"and".equalsIgnoreCase(token) || "or".equalsIgnoreCase(token) || "like".equalsIgnoreCase(token) || "in".equalsIgnoreCase(token) ||
					"between".equalsIgnoreCase(token) || "not".equalsIgnoreCase(token) || "starts-with".equalsIgnoreCase(token) ||
					"ends-with".equalsIgnoreCase(token) || "contains".equalsIgnoreCase(token)) {
				return TokenType.comp;
			}
			if (token.length() > 0) {
				return TokenType.param;
			}
			return null;
		}
		
		Comparison getComparison() {
			if (type == TokenType.comp) {
				switch (token.toLowerCase()) {
					case "=": return Comparison.EQ;
					case "!=": return Comparison.NE;
					case "<": return Comparison.LT;
					case "<=": return Comparison.LE;
					case ">": return Comparison.GT;
					case ">=": return Comparison.GE;
					case "and": return Comparison.AND;
					case "or": return Comparison.OR;
					case "like": return Comparison.LIKE;
					case "in": return Comparison.IN;
					case "not": return Comparison.NOT;
					case "between": return Comparison.BETWEEN;
					case "starts-with": return Comparison.SW;
					case "ends-with": return Comparison.EW;
					case "contains": return Comparison.CNT;
				}
			}
			return null;
		}
		
		@Override
		public String toString() {
			return type + ": " + token;
		}
		
	}
	
}
