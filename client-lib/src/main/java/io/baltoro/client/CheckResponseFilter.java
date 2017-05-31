package io.baltoro.client;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response.StatusType;

import io.baltoro.to.APIError;

public class CheckResponseFilter implements ClientResponseFilter
{
 
	
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) 
	throws IOException
	{
		int status = responseContext.getStatus();
		StatusType statusType = responseContext.getStatusInfo();
		
		
		
		String error = responseContext.getHeaderString("api-error");
		
		if(status == 455)
		{
			//System.out.println(error);
			throw new APIError(error);
		}
	}
	
}