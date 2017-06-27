package io.baltoro.to;

import java.util.Map;

public class ResponseContext
{

	private String redirect;
	private String error;
	private String sessionId;
	private String mimeType;
	private Map<String, String> headers; 
	private Map<String, String> cookies;
	private byte[] data;
	
	
	public String getRedirect()
	{
		return redirect;
	}
	public void setRedirect(String redirect)
	{
		this.redirect = redirect;
	}
	public String getError()
	{
		return error;
	}
	public void setError(String error)
	{
		this.error = error;
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

	public byte[] getData()
	{
		return data;
	}
	public void setData(byte[] data)
	{
		this.data = data;
	}
	public String getMimeType()
	{
		return mimeType;
	}
	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}
	
	
	
	
	
}
