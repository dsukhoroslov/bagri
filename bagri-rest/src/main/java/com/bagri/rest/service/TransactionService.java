package com.bagri.rest.service;

import javax.inject.Singleton;
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

import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.TransactionIsolation;
import com.bagri.xdm.api.TransactionManagement;

@Singleton
@Path("/tx")
public class TransactionService extends RestService {
	
    private TransactionManagement getTxManager() {
    	SchemaRepository repo = getRepository();
    	if (repo != null) {
        	return repo.getTxManagement();
    	}
		return null;
    }
    
	@GET
    @Produces(MediaType.TEXT_PLAIN) 
    public boolean getTxState() {
		TransactionManagement txMgr = getTxManager();
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
		TransactionManagement txMgr = getTxManager();
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
		TransactionManagement txMgr = getTxManager();
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
		TransactionManagement txMgr = getTxManager();
    	try {
    		txMgr.rollbackTransaction(txId);
            return Response.ok().build();
    	} catch (Exception ex) {
    		logger.error("deleteTx.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    	}
    }
    
}