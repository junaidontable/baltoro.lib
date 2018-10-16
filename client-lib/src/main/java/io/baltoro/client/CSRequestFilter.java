package io.baltoro.client;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.NewCookie;

public class CSRequestFilter implements ClientRequestFilter
{
 
	static Logger log = Logger.getLogger(CSRequestFilter.class.getName());
	private String appName;
	
	public CSRequestFilter(String appName)
	{
		this.appName = appName;
	}
	
	public void filter(ClientRequestContext requestContext) 
	throws IOException
	{
		//requestContext.getHeaders().add("BLT_APP_UUID", Baltoro.appUuid);
		requestContext.getHeaders().add("BLT_APP_NAME", appName);
		//requestContext.getHeaders().add("BLT_SERVICE_NAME", Baltoro.serviceNames.toString());
		
		StringBuffer value = new StringBuffer();
		
		
		Map<String, String> ccookies = Baltoro.userRequestCtx.get().getCookies();
		for (String c : ccookies.keySet())
		{
			String v = ccookies.get(c);
			value.append(c+"="+v+";");
		}
		
		/*
		UserSession session = Baltoro.getUserSession();
		Set<NewCookie> cookies = session.getCookies();
		
		
		for (NewCookie cookie : cookies)
		{
			value.append(cookie.getName()+"="+cookie.getValue()+";");
		}
		*/
		//System.out.println("cookie:"+value.toString());
		
		requestContext.getHeaders().add("Cookie", value.toString());
		
	}
	

	
}