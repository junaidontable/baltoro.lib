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

public class CSResponseFilter implements ClientResponseFilter
{
 
	static Logger log = Logger.getLogger(CSResponseFilter.class.getName());
	private String appName;
	
	
	public CSResponseFilter(String appName)
	{
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
			
		UserSession userSession = Baltoro.getUserSession();
		
		
		Map<String, NewCookie> map = context.getCookies();
		for (String key : map.keySet())
		{
			NewCookie cookie = map.get(key);
			//log.info("received ======<"+this.appName+">======= >>>["+map.hashCode()+"]>> 111 >>>>>> "+key+" : "+cookie);
			userSession.addCookie(cookie.getName(), cookie.getValue());
		}	
	}
	
}