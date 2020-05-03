package io.baltoro;

import java.io.Serializable;
import java.util.Map;


public class Response implements Serializable
{


	private static final long serialVersionUID = 1L;


	public String reqUuid;
	private String contentType;

	

	public Map<String, String> headers;
	

	public Map<String, String> cookies;
	

	public byte[] data;
	

	public String error;

	public String getReqUuid()
	{
		return reqUuid;
	}

	public void setReqUuid(String reqUuid)
	{
		this.reqUuid = reqUuid;
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

	public String getError()
	{
		return error;
	}

	public void setError(String error)
	{
		this.error = error;
	}

	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

}
