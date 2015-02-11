package com.bagri.common.stats;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class InvocationStatsHandler implements InvocationHandler {

	private Object target; 
	private InvocationStatistics stats;
	
	protected InvocationStatsHandler(Object target, InvocationStatistics stats) {
		this.target = target;
		this.stats = stats;
		initStats(stats);
	}
	
	protected abstract void initStats(InvocationStatistics stats);
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        Object result = null;

        Throwable ex = null;
        boolean failed = false;
        long stamp = System.currentTimeMillis(); //nanoTime();
        //getLogger().enter(name);
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
        //stamp = System.nanoTime() - stamp;
        stamp = System.currentTimeMillis() - stamp;
        stats.updateStats(name, !failed, stamp);
        if (failed) {
            throw ex;
        }
        return result;
	}

}
