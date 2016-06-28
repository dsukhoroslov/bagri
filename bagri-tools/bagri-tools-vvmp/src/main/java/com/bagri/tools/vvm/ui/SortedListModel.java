package com.bagri.tools.vvm.ui;

import javax.swing.*;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:roman.orekhov@db.com">Roman Orekhov</a>
 *         Date: 31.07.14.
 */
public class SortedListModel<E> extends AbstractListModel<E> {

    SortedSet<E> model;

    public SortedListModel() {
        model = new TreeSet<E>();
    }

    public int getSize() {
        return model.size();
    }

    public E getElementAt(int index) {
        return (E) model.toArray()[index];
    }

    public void add(E element) {
        if (model.add(element)) {
            fireContentsChanged(this, 0, getSize());
        }
    }

    public void addAll(List<E> elements) {
//        Collection c = Arrays.asList(elements);
        model.addAll(elements);
        fireContentsChanged(this, 0, getSize());
    }

    public void clear() {
        model.clear();
        fireContentsChanged(this, 0, getSize());
    }

    public boolean contains(E element) {
        return model.contains(element);
    }

    public E firstElement() {
        return model.first();
    }

    public Iterator<E> iterator() {
        return model.iterator();
    }

    public E lastElement() {
        return model.last();
    }

    public boolean removeElement(E element) {
        boolean removed = model.remove(element);
        if (removed) {
            fireContentsChanged(this, 0, getSize());
        }
        return removed;
    }
}
