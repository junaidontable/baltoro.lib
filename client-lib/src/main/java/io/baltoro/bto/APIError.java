package io.baltoro.bto;

import javax.ws.rs.WebApplicationException;

public class APIError extends WebApplicationException
{
	
	
	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public APIError(String message) 
	{
		//Response response = Response.status(455).entity(message).type("text/plain").build();
		super(message, 455);
		
	}
	  
	
	
}
