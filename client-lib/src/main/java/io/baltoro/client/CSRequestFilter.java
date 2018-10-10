package io.baltoro.client;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

public class CSRequestFilter implements ClientRequestFilter
{
 
	static Logger log = Logger.getLogger(CSRequestFilter.class.getName());
	
	private Map<String, NewCookie> cMap;

	public CSRequestFilter(Map<String, NewCookie> cMap)
	{
		this.cMap = cMap;
	}
	
	public void filter(ClientRequestContext requestContext) 
	throws IOException
	{
		requestContext.getHeaders().add("BLT_APP_UUID", Baltoro.appUuid);
		requestContext.getHeaders().add("BLT_APP_NAME", Baltoro.appName);
		requestContext.getHeaders().add("BLT_SERVICE_NAME", Baltoro.serviceNames.toString());
		
		for (String key : cMap.keySet())
		{
			NewCookie cookie = cMap.get(key);
			log.info("sending ======>======= >]>> 111 >>>>>> "+key+" : "+cookie);
			requestContext.getCookies().put(cookie.getName(), new Cookie(cookie.getName(),cookie.getValue()));
		}
		
		
	}
	

	
}