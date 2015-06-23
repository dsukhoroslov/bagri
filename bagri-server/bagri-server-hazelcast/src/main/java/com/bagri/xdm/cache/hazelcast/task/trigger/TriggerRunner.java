package com.bagri.xdm.cache.hazelcast.task.trigger;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.cache.hazelcast.impl.TriggerManagementImpl;
import com.bagri.xdm.domain.XDMDocument;
//import com.bagri.xdm.domain.XDMTrigger;
import com.bagri.xdm.system.XDMTriggerAction.Action;
import com.bagri.xdm.system.XDMTriggerAction.Scope;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_RunTriggerTask;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

@SpringAware
public class TriggerRunner implements Callable<Void>, IdentifiedDataSerializable { //Runnable

	private static final transient Logger logger = LoggerFactory.getLogger(TriggerRunner.class);

	private Action action;
	private Scope scope;
	private int index;
	private XDMDocument xDoc;
	private String clientId;
	private TriggerManagementImpl trManager;

	public TriggerRunner() {
		// for de-ser
	}
	
	public TriggerRunner(Action action, Scope scope, int index, XDMDocument xDoc, String clientId) {
		this.action = action;
		this.scope = scope;
		this.index = index;
		this.xDoc = xDoc;
		this.clientId = clientId;
	}
		
    @Autowired
	public void setTriggerManager(TriggerManagementImpl trManager) {
    	this.trManager = trManager;
    }
    
	@Override
	public Void call() {
		try {
			trManager.runTrigger(action, scope, xDoc, index, clientId);
		} catch (Throwable ex) {
			logger.error("runTrigger.error", ex);
		}
		return null;
	}
	
	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_RunTriggerTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		action = Action.valueOf(in.readUTF());
		scope = Scope.valueOf(in.readUTF());
		index = in.readInt();
		xDoc = in.readObject();
		clientId = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(action.name());
		out.writeUTF(scope.name());
		out.writeInt(index);
		out.writeObject(xDoc);
		out.writeUTF(clientId);
	}

}
