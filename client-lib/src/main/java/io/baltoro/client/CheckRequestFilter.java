package io.baltoro.client;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.NewCookie;

public class CheckRequestFilter implements ClientRequestFilter 
{
 
	protected Map<String, NewCookie> cookieMap;
	
		
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