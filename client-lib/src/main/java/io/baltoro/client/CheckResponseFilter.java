package io.baltoro.client;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response.StatusType;

import io.baltoro.to.APIError;

public class CheckResponseFilter implements ClientResponseFilter
{
 
	static Logger log = Logger.getLogger(CheckResponseFilter.class.getName());
	
	
	protected Map<String, NewCookie> cookieMap;
	private String appName;
	
	public CheckResponseFilter(String appName, Map<String, NewCookie> cookieMap)
	{
		this.cookieMap = cookieMap;
		this.appName = appName;
	}
	
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) 
	throws IOException
	{
		
		receiveCookies(responseContext);
		int status = responseContext.getStatus();
		StatusType statusType = responseContext.getStatusInfo();
		
		
		
		String error = responseContext.getHeaderString("api-error");
		
		if(status == 455)
		{
			//System.out.println(error);
			throw new APIError(error);
		}
	}
	
	void receiveCookies(ClientResponseContext context)
	{
			
		Map<String, NewCookie> map = context.getCookies();
		for (String key : map.keySet())
		{
			NewCookie cookie = map.get(key);
			log.info("received ======<"+this.appName+">======= >>>["+map.hashCode()+"]>> 111 >>>>>> "+key+" : "+cookie);
			cookieMap.put(key, cookie);
		}	
	}
	
}