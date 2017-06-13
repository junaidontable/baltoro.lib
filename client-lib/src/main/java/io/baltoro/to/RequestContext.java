package io.baltoro.to;

import java.util.Map;

public class RequestContext
{

	public String url;
	public String ip;
	public String sessionId;
	public Map<String, String> headers; 
	public Map<String, String> cookies;
	public Principal principal;
	public Map<String,String[]> requestParams;
	
	
	

	
	
	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public String getIp()
	{
		return ip;
	}
	public void setIp(String ip)
	{
		this.ip = ip;
	}
	public String getSessionId()
	{
		return sessionId;
	}
	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}
	public Map<String, String> getHeaders()
	{
		return headers;
	}
	public void setHeaders(Map<String, String> headers)
	{
		this.headers = headers;
	}
	public Map<String, String> getCookies()
	{
		return cookies;
	}
	public void setCookies(Map<String, String> cookies)
	{
		this.cookies = cookies;
	}
	public Principal getPrincipal()
	{
		return principal;
	}
	public void setPrincipal(Principal principal)
	{
		this.principal = principal;
	}
	
	
	
	
	
}
