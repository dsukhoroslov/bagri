package com.bagri.common.query;

public enum Comparison {
	
	EQ,
	NE,
	LT,
	LE,
	GT,
	GE,
	IN,
	LIKE,
	BETWEEN,
	
	AND,
	OR,
	NOT;
	
	public static Comparison negate(Comparison comp) {
		switch (comp) {
			case EQ: return Comparison.NE;
			case NE: return Comparison.EQ;
			case LE: return Comparison.GT;
			case LT: return Comparison.GE;
			case GE: return Comparison.LT;
			case GT: return Comparison.LE;
			default: return comp;
		}
	}
	
	public static boolean isBinary(Comparison compType) {
		switch (compType) {
			case AND:
			case OR: return true;
			//case NOT:
			//	return -1;
			default: return false;
		}
	}

}
