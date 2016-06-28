package com.bagri.tools.vvm.event;

import javax.swing.event.EventListenerList;

public class EventBus <E extends Event> {
    private EventListenerList listeners = new EventListenerList();

    public <T extends EventHandler<E>> void addEventHandler(T handler) {
        if (null == handler) {
            return;
        }
        listeners.add((Class<T>) handler.getClass(), handler);
    }

    public <T extends EventHandler<E>> void removeEventHandler(T handler) {
        if (null == handler) {
            return;
        }
        listeners.remove((Class<T>) handler.getClass(), handler);
    }

    // Notify all listeners that have registered interest for
    // notification on this event type.  The event instance
    // is lazily created using the parameters passed into
    // the fire method.

    public void fireEvent(E evt) {
        if (null == evt) {
            return;
        }
        // Guaranteed to return a non-null array
        Object[] listenersArray = listeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listenersArray.length-2; i>=0; i-=2) {
//            if (listenersArray[i] == ApplicationEventListener.class) {
                ((EventHandler<E>)listenersArray[i+1]).handleEvent(evt);
//            }
        }
    }
}
