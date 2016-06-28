package com.bagri.xquery.saxon.extension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.util.ReflectUtils;
import com.bagri.xdm.system.Function;
import com.bagri.xdm.system.Parameter;

import static com.bagri.xquery.saxon.SaxonUtils.*;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;

@SuppressWarnings("rawtypes")
public class StaticFunctionExtension extends ExtensionFunctionDefinition {
	
	private static final Logger logger = LoggerFactory.getLogger(StaticFunctionExtension.class);
	
	private Function xdf;
	private Class[] params;
	private Configuration config;
	
	public StaticFunctionExtension() {
		// de-serialization ?
	}
	
	public StaticFunctionExtension(Function xdf, Configuration config) throws ClassNotFoundException {
		this.xdf = xdf;
		this.config = config;
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
		for (Parameter xdp: xdf.getParameters()) {
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
					logger.trace("args2Params; args[{}]: {}:{}", i, result[i].getClass().getName(), result[i]);
				}
				return result;
			}
			
			private Object sequence2Object(Sequence seq) throws XPathException {
				return itemToObject(seq.head()); 
			}
			
			private Sequence object2Sequence(Object value) throws XPathException {
				return objectToItem(value, StaticFunctionExtension.this.config);
			}

			
			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				try {
					Class cls = Class.forName(xdf.getClassName());
					Method m = cls.getMethod(xdf.getMethod(), params);
					m.setAccessible(true);
					logger.trace("call; invoking: {}; params: {}", m, params);
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
		for (Parameter xp: xdf.getParameters()) {
			result[idx] = ReflectUtils.type2Class(xp.getType());
			idx++;
		}
		return result;
	}
	
}
