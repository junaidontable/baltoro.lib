package io.baltoro.client;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

public class CheckRequestFilter implements ClientRequestFilter 
{
 
	
    public void filter(ClientRequestContext requestContext)
    throws IOException 
    {
    	/*
        if (requestContext.getHeaders().get("Client-Name") == null) 
        {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                .entity("Client-Name header must be defined.")
                        .build());
         }
         */
    }
}