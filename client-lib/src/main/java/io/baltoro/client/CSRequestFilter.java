package io.baltoro.client;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

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
		requestContext.getHeaders().add("BLT_APP_NAME", appName);
		
		StringBuffer value = new StringBuffer();
		
		
		Map<String, String> ccookies = Baltoro.userRequestCtx.get().getCookies();
		for (String c : ccookies.keySet())
		{
			String v = ccookies.get(c);
			value.append(c+"="+v+";");
		}
			
		requestContext.getHeaders().add("Cookie", value.toString());
		
	}
	

	
}