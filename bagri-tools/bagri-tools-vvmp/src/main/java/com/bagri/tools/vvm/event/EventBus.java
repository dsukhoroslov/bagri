package com.bagri.tools.vvm.event;

import java.awt.EventQueue;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectionNotification;
import javax.swing.event.EventListenerList;

import com.bagri.tools.vvm.model.BagriManager;
import com.bagri.tools.vvm.model.ClusterManagement;
import com.bagri.tools.vvm.model.SchemaManagement;
import com.bagri.tools.vvm.model.UserManagement;

public class EventBus implements NotificationListener {
	
	private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());
	
    private MBeanServerConnection mbsc;
    private EventListenerList listeners = new EventListenerList();
    
    public EventBus(MBeanServerConnection mbsc) {
    	this.mbsc = mbsc;
    	// subscribe..
    	try {
    		// we should subscribe to all schemas somehow!
	    	ObjectName oName = new ObjectName("com.bagri.db:type=Schema,name=XDM");
	    	mbsc.addNotificationListener(oName, this, null,  null);
    	} catch (Exception ex) {
            LOGGER.throwing(this.getClass().getName(), "<init>", ex);
    	}
    }

    public void addEventHandler(EventHandler handler) {
        if (null == handler) {
            return;
        }
        listeners.add(EventHandler.class, handler);
    }

    public <T extends EventHandler<Event>> void removeEventHandler(T handler) {
        if (null == handler) {
            return;
        }
        listeners.remove((Class<T>) handler.getClass(), handler);
    }

    // Notify all listeners that have registered interest for
    // notification on this event type.  The event instance
    // is lazily created using the parameters passed into
    // the fire method.

    public void fireEvent(Event evt) {
        if (null == evt) {
            return;
        }
        
        // Guaranteed to return a non-null array
        Object[] listenersArray = listeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listenersArray.length-2; i>=0; i-=2) {
//            if (listenersArray[i] == ApplicationEventListener.class) {
                ((EventHandler<Event>) listenersArray[i+1]).handleEvent(evt);
//            }
        }
    }

    /* notification listener:  handleNotification */
    @Override
    public void handleNotification(final Notification notification, Object handback) {
    	LOGGER.info("EventBus; got notification: " + notification); 
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (JMXConnectionNotification.FAILED.equals(notification.getType()) || JMXConnectionNotification.CLOSED.equals(notification.getType())) {
                    //dispose();
                    fireEvent(new ApplicationEvent(this, BagriManager.MANAGER_STATE_CHANGED));
                } else if (notification.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
                    ObjectName mbean = ((MBeanServerNotification) notification).getMBeanName();
                    if ("User".equals(mbean.getKeyProperty("type"))) {
                        fireEvent(new ApplicationEvent(this, UserManagement.USER_STATE_CHANGED));
                    } else if ("Node".equals(mbean.getKeyProperty("type"))) {
                        fireEvent(new ApplicationEvent(this, ClusterManagement.CLUSTER_STATE_CHANGED));
                    } else if ("Schema".equals(mbean.getKeyProperty("type"))) {
                        fireEvent(new ApplicationEvent(this, SchemaManagement.SCHEMA_STATE_CHANGED));
                    }
                } else if (notification.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION)) {
                    ObjectName mbean = ((MBeanServerNotification) notification).getMBeanName();
                    if ("User".equals(mbean.getKeyProperty("type"))) {
                        fireEvent(new ApplicationEvent(this, UserManagement.USER_STATE_CHANGED));
                    } else if ("Node".equals(mbean.getKeyProperty("type"))) {
                        fireEvent(new ApplicationEvent(this, ClusterManagement.CLUSTER_STATE_CHANGED));
                    } else if ("Schema".equals(mbean.getKeyProperty("type"))) {
                        fireEvent(new ApplicationEvent(this, SchemaManagement.SCHEMA_STATE_CHANGED));
                    }
                } else {
                    fireEvent(new ApplicationEvent(notification.getUserData(), notification.getType()));
                }
            }
        });

    }

	
}
