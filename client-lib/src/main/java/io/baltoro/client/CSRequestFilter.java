package io.baltoro.client;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import io.baltoro.client.util.StringUtil;

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
		
		StringBuffer cookieValue = new StringBuffer();
		
		UserSession session = Baltoro.getUserSession();
		
		Map<String, String> cookies = Baltoro.userRequestCtx.get().getCookies();
		for (String c : cookies.keySet())
		{
			String v = cookies.get(c);
			String sessionValue = session.getCookies().get(c);
			
			if(StringUtil.isNotNullAndNotEmpty(sessionValue))
			{
				v = sessionValue;
			}
					
					
			cookieValue.append(c+"="+v+";");
		}
			
		requestContext.getHeaders().add("Cookie", cookieValue.toString());
		
	}
	

	
}