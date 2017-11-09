package com.bagri.server.hazelcast.task.trigger;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_factory_id;
import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_ExecuteTriggerTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.model.Transaction;
import com.bagri.core.system.TriggerAction.Order;
import com.bagri.core.system.TriggerAction.Scope;
import com.bagri.server.hazelcast.impl.TriggerManagementImpl;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class TriggerExecutor implements Runnable, Callable<Void>, IdentifiedDataSerializable { 

	private static final transient Logger logger = LoggerFactory.getLogger(TriggerRunner.class);

	private Order order;
	private Scope scope;
	private int index;
	private Transaction xTx;
	private String clientId;
	private TriggerManagementImpl trManager;

	public TriggerExecutor() {
		// for de-ser
	}
	
	public TriggerExecutor(Order order, Scope scope, int index, Transaction xTx, String clientId) {
		this.order = order;
		this.scope = scope;
		this.index = index;
		this.xTx = xTx;
		this.clientId = clientId;
	}
		
    @Autowired
	public void setTriggerManager(TriggerManagementImpl trManager) {
    	this.trManager = trManager;
    }
    
	@Override
	public Void call() {
		try {
			trManager.runTrigger(order, scope, xTx, index, clientId);
		} catch (Throwable ex) {
			logger.error("call.error", ex);
		}
		return null;
	}
	
	@Override
	public void run() {
		try {
			trManager.runTrigger(order, scope, xTx, index, clientId);
		} catch (Throwable ex) {
			logger.error("run.error", ex);
		}
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_ExecuteTriggerTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		order = Order.values()[in.readInt()];
		scope = Scope.values()[in.readInt()];
		index = in.readInt();
		xTx = in.readObject();
		clientId = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(order.ordinal());
		out.writeInt(scope.ordinal());
		out.writeInt(index);
		out.writeObject(xTx);
		out.writeUTF(clientId);
	}

}


