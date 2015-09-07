package com.bagri.common.stats;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.stats.watch.StopWatch;

public abstract class InvocationStatsHandler implements InvocationHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Object target; 
	private StopWatch stopWatch;
	private BlockingQueue<StatisticsEvent> queue;
	
	protected InvocationStatsHandler(Object target, BlockingQueue<StatisticsEvent> queue) {
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
        try {
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
        long stamp = stopWatch.stop();
        if (!queue.offer(new StatisticsEvent(name, !failed, stamp))) {
        	logger.warn("invoke: the queue is full!!");
        }
        if (failed) {
            throw ex;
        }
        return result;
	}

}
