package com.bagri.rest.service;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.TransactionIsolation;
import com.bagri.xdm.api.TransactionManagement;


@Path("/tx")
public class TransactionService {
	
    private static final transient Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Inject
    private RepositoryProvider repos;
    
    private TransactionManagement getTxManager(String schemaName) {
    	if (repos == null) {
    		logger.warn("getTxManager; resource not initialized: RepositoryProvider is null");
    		return null;
    	}
    	SchemaRepository repo = repos.getRepository(schemaName);
    	if (repo == null) {
    		logger.warn("getTxManager; Repository is not active for schema {}", schemaName);
    		return null;
    	}
    	return repo.getTxManagement();
    }
    
	@GET
    @Produces(MediaType.TEXT_PLAIN) 
    public boolean getTxState() {
		String schema = "default";
		TransactionManagement txMgr = getTxManager(schema);
    	try {
            return txMgr.isInTransaction();
    	} catch (Exception ex) {
    		logger.error("getTxState.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    	}
    }
    
    @POST
    @Produces(MediaType.TEXT_PLAIN) 
	public long postTx(String isolation) {
		String schema = "default";
		TransactionManagement txMgr = getTxManager(schema);
    	try {
    		long txId = txMgr.beginTransaction(TransactionIsolation.valueOf(isolation));
            return txId; //Response.ok(txId).build();
    	} catch (Exception ex) {
    		logger.error("postTx.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    	}
    }

    @PUT
    @Path("/{txId}")
    @Produces(MediaType.TEXT_PLAIN) 
	public Response putTx(@PathParam("txId") long txId) {
		String schema = "default";
		TransactionManagement txMgr = getTxManager(schema);
    	try {
    		txMgr.commitTransaction(txId);
            return Response.ok().build();
    	} catch (Exception ex) {
    		logger.error("putTx.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    	}
    }
    
    @DELETE
    @Path("/{txId}")
    @Produces(MediaType.TEXT_PLAIN) 
	public Response deleteTx(@PathParam("txId") long txId) {
		String schema = "default";
		TransactionManagement txMgr = getTxManager(schema);
    	try {
    		txMgr.rollbackTransaction(txId);
            return Response.ok().build();
    	} catch (Exception ex) {
    		logger.error("deleteTx.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    	}
    }
    
}