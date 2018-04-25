package com.bagri.xquery.saxon;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.functions.DeepEqual;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.om.AbstractItem;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.z.IntSet;

public class ArrayItemImpl extends AbstractItem implements ArrayItem {
	
	private Collection<Object> source;
	
	public ArrayItemImpl(Collection<Object> source) {
		this.source = source;
	}

	@Override
	public boolean isMap() {
		return false;
	}

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public FunctionItemType getFunctionItemType() {
        return ArrayItemType.ANY_ARRAY_TYPE;
	}

	@Override
	public StructuredQName getFunctionName() {
		return null;
	}

	@Override
	public int getArity() {
		return 1;
	}

	@Override
	public OperandRole[] getOperandRoles() {
        return new OperandRole[] {OperandRole.SINGLE_ATOMIC};
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
        return null; //get((int) ((IntegerValue) args[0].head()).longValue() - 1);
	}

	@Override
	public boolean deepEquals(Function other, XPathContext context, AtomicComparer comparer, int flags)	throws XPathException {
        if (other instanceof ArrayItem) {
            ArrayItem that = (ArrayItem) other;
            if (this.size() != that.size()) {
                return false;
            }
            for (int i = 0; i < this.size(); i++) {
                if (!DeepEqual.deepEqual(this.get(i).iterate(), that.get(i).iterate(), comparer, context, flags)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
	}

	@Override
	public String getDescription() {
		return "array";
	}

	@Override
	public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("array");
        out.emitAttribute("size", size() + "");
        //for (Sequence mem : members) {
        //    Literal.exportValue(mem, out);
        //}
        out.endElement();
	}

	@Override
	public boolean isTrustedResultType() {
		return false;
	}

	@Override
	public String getStringValue() {
        throw new UnsupportedOperationException("An array does not have a string value");
	}

	@Override
	public CharSequence getStringValueCS() {
        throw new UnsupportedOperationException("An array does not have a string value");
	}

	@Override
	public AtomicSequence atomize() throws XPathException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean effectiveBooleanValue() throws XPathException {
        throw new XPathException("Effective boolean value is not defined for arrays");
	}

	@Override
	public Sequence get(int index) throws XPathException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		return source.size();
	}

	@Override
	public boolean isEmpty() {
		return source.isEmpty();
	}

	@Override
	public Iterator<Sequence> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayItem concat(ArrayItem other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayItem remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayItem removeSeveral(IntSet positions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SequenceType getMemberType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Sequence> getMembers() {
		// TODO Auto-generated method stub
		return null;
	}

}
