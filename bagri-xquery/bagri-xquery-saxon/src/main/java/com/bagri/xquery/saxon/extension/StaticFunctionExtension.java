package com.bagri.xquery.saxon.extension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.system.XDMFunction;
import com.bagri.xdm.system.XDMParameter;
import com.bagri.xdm.system.XDMType;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

@SuppressWarnings("rawtypes")
public class StaticFunctionExtension extends ExtensionFunctionDefinition {
	
	private static final Logger logger = LoggerFactory.getLogger(StaticFunctionExtension.class);
	
	private XDMFunction xdf;
	private Class[] params;
	
	public StaticFunctionExtension() {
		// de-serialization ?
	}
	
	public StaticFunctionExtension(XDMFunction xdf) throws ClassNotFoundException {
		this.xdf = xdf;
		params = buildParams();
	}

	@Override
	public StructuredQName getFunctionQName() {
		//return new StructuredQName(bg_schema, bg_ns, "store-document");
		return new StructuredQName(xdf.getPrefix(), xdf.getClassName(), xdf.getMethod());
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		SequenceType[] result = new SequenceType[xdf.getParameters().size()];
		int idx = 0;
		for (XDMParameter xdp: xdf.getParameters()) {
			result[idx] = type2Sequence(xdp);
			idx++;
		}
		return result;
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return type2Sequence(xdf.getResult());
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		
		return new ExtensionFunctionCall() {

			private Object[] args2Params(Sequence[] args) throws XPathException {
				Object[] result = new Object[args.length];
				for (int i=0; i < args.length; i++) {
					result[i] = sequence2Object(args[i]);
				}
				return result;
			}
			
			private Object sequence2Object(Sequence seq) throws XPathException {
				return seq.iterate().next();
			}
			
			private Sequence object2Sequence(Object value) {
				return null; //new Sequence();
			}

			
			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				try {
					Class cls = Class.forName(xdf.getClassName());
					Method m = cls.getMethod(xdf.getMethod(), params);
					m.setAccessible(true);
					Object result = m.invoke(null, args2Params(arguments));
					return object2Sequence(result);
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | 
						IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
					
					throw new XPathException(ex);
				}
			}
		};
		
	}
	
	private Class[] buildParams() throws ClassNotFoundException {
		Class[] result = new Class[xdf.getParameters().size()];
		int idx = 0;
		for (XDMParameter xp: xdf.getParameters()) {
			result[idx] = type2Class(xp.getType());
			idx++;
		}
		return result;
	}
	
	private static SequenceType type2Sequence(XDMType type) {
		ItemType it = type2Item(type.getType());
		int cardinality;
		switch (type.getCardinality()) {
			case one_or_more: cardinality = StaticProperty.ALLOWS_ONE_OR_MORE; break; 
			case zero_or_one: cardinality = StaticProperty.ALLOWS_ZERO_OR_ONE; break; 
			case zero_or_more: cardinality = StaticProperty.ALLOWS_ZERO_OR_MORE; break;
			default: cardinality = StaticProperty.ALLOWS_ONE;  
		}
		return SequenceType.makeSequenceType(it, cardinality);
	}
	
	private static ItemType type2Item(String type) {

		// provide long switch here..
		switch (type) {
			case "boolean": return BuiltInAtomicType.BOOLEAN; 
			case "byte": return BuiltInAtomicType.BYTE; 
			case "double": return BuiltInAtomicType.DOUBLE; 
			case "float": return BuiltInAtomicType.FLOAT; 
			case "int": return BuiltInAtomicType.INT; 
			case "integer": return BuiltInAtomicType.INTEGER; 
			case "long": return BuiltInAtomicType.LONG; 
			case "short": return BuiltInAtomicType.SHORT; 
			case "string": return BuiltInAtomicType.STRING; 
		}
		return BuiltInAtomicType.ANY_ATOMIC; 
	}
	
	private static Class type2Class(String type) throws ClassNotFoundException {
		switch (type) {
			case "boolean": return boolean.class; 
			case "byte": return byte.class; 
			case "double": return double.class; 
			case "float": return float.class; 
			case "int": return int.class; 
			case "integer": return int.class; 
			case "long": return long.class; 
			case "short": return short.class; 
			case "string": return java.lang.String.class; 
		}
		return Class.forName(type);
	}

}
