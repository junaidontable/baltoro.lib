package io.baltoro.bto;

import java.util.Map;

public class RequestContext
{

	private String url;
	private String ip;
	private String sessionId;
	private String method;
	private Map<String, String> headers; 
	private Map<String, String> cookies;
	private Map<String,String[]> requestParams;
	private byte[] data;
	private String apiPath;
	private String relativePath;
	private boolean authRequired;
	private String roles;
	

	
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
	
	public Map<String, String[]> getRequestParams()
	{
		return requestParams;
	}
	public void setRequestParams(Map<String, String[]> requestParams)
	{
		this.requestParams = requestParams;
	}
	public byte[] getData()
	{
		return data;
	}
	public void setData(byte[] data)
	{
		this.data = data;
	}
	public String getApiPath()
	{
		return apiPath;
	}
	public void setApiPath(String apiPath)
	{
		this.apiPath = apiPath;
	}
	public String getRelativePath()
	{
		return relativePath;
	}
	public void setRelativePath(String relativePath)
	{
		this.relativePath = relativePath;
	}
	public String getMethod()
	{
		return method;
	}
	public void setMethod(String method)
	{
		this.method = method;
	}
	public boolean isAuthRequired()
	{
		return authRequired;
	}
	public void setAuthRequired(boolean authRequired)
	{
		this.authRequired = authRequired;
	}
	public String getRoles()
	{
		return roles;
	}
	public void setRoles(String roles)
	{
		this.roles = roles;
	}
	
	
	
	
	
	
	
}
