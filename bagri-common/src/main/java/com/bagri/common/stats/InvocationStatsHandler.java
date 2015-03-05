package com.bagri.common.stats;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.manage.JMXUtils;

public abstract class InvocationStatsHandler implements InvocationHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Object target; 
	private StopWatch stopWatch;
	private BlockingQueue<InvocationEvent> queue;
	
	protected InvocationStatsHandler(Object target, BlockingQueue<InvocationEvent> queue) {
		this.target = target;
		this.queue = queue;
	}
	
	public void setStopWatch(StopWatch stopWatch) {
		this.stopWatch = stopWatch;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        Object result = null;

        Throwable ex = null;
        boolean failed = false;
        stopWatch.start();
        //long stamp = System.currentTimeMillis(); //nanoTime();
        try {
            //result = method.invoke(proxy, args);
            result = method.invoke(target, args);
        } catch (InvocationTargetException ite) {
            failed = true;
            if (ite.getCause() != null) {
                ex = ite.getCause();
            } else {
                ex = ite;
            }
        } catch (Throwable t) {
            failed = true;
            ex = t;
        }
        //stamp = System.currentTimeMillis() - stamp;
        long stamp = stopWatch.stop();
        if (!queue.offer(new InvocationEvent(name, !failed, stamp))) {
        	logger.warn("invoke: the queue is full!!");
        }
        if (failed) {
            throw ex;
        }
        return result;
	}

}
