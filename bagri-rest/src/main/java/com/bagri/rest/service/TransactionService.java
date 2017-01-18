package com.bagri.rest.service;

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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;

import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.api.TransactionManagement;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * JAX-RS transactions resource, contains methods for transactions management. Accessible on /tx path. 
 * 
 * @author Denis Sukhoroslov
 *
 */
@Path("/tx")
@Api(value = "transactions")
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
	@ApiOperation(value = "getTxState: return transaction state of the current user session")
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
	@ApiOperation(value = "postTx: starts a new user Transaction in the current session")
	public Response postTx(String isolation) {
		TransactionManagement txMgr = getTxManager();
    	try {
    		long txId = txMgr.beginTransaction(TransactionIsolation.valueOf(isolation));
            return Response.created(UriBuilder.fromPath("/tx/" + txId).build()).entity(txId).build();
    	} catch (Exception ex) {
    		logger.error("postTx.error", ex);
    		throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
    	}
    }

    @PUT
    @Path("/{txId}")
    @Produces(MediaType.TEXT_PLAIN) 
	@ApiOperation(value = "putTx: commits the specified user Transaction")
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
	@ApiOperation(value = "deleteTx: rolls back the specified user Transaction")
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