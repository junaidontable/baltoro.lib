package io.baltoro.client;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

public class HttpRequestPollerHeaderHandler implements ClientRequestFilter
{
 
	static Logger log = Logger.getLogger(HttpRequestPollerHeaderHandler.class.getName());
	

	public HttpRequestPollerHeaderHandler()
	{
		
	}
	
	public void filter(ClientRequestContext requestContext) 
	throws IOException
	{
		
		requestContext.getHeaders().add("BLT_APP_UUID", Baltoro.appUuid);
		
		requestContext.getHeaders().add("BLT_INSTANCE_UUID", Baltoro.instanceUuid);
		requestContext.getHeaders().add("BLT_TOKEN", Baltoro.hostId);
		requestContext.getHeaders().add("BLT_APP_NAME", Baltoro.appName);
		requestContext.getHeaders().add("BLT_SERVICE_NAME", Baltoro.serviceNames.toString());
	}
	

	
}