package io.baltoro;

import java.util.HashMap;
import java.util.Map;

public class Reply
{

	public String contentType;
	public String error;
	public byte[] data;
	public Map<String,String> cookies = new HashMap<String, String>();
	public Map<String,String> headers = new HashMap<String, String>();
		
	public static Reply contentType(String contentType)
	{
		Reply r = new Reply();
		r.contentType = contentType;
		return r;
	}
	
	public static Reply error(String error)
	{
		throw new RuntimeException(error);
		/*
		Reply r = new Reply();
		
		r.error = error;
		return r;
		*/
	}
	
	public static Reply data(String data)
	{
		Reply r = new Reply();
		r.data = data.getBytes();
		return r;
	}
	
	public static Reply data(byte[] data)
	{
		Reply r = new Reply();
		r.data = data;
		return r;
	}
	
	public void addCookie(String name,String val)
	{
		cookies.put(name,val);
	}
	
	public Reply setData(String data)
	{
		this.data = data.getBytes();
		return this;
	}
	
	public Reply setData(byte[] data)
	{
		this.data = data;
		return this;
	}
		
	public Reply setError(String error)
	{
		this.error = error;
		return this;
	}
	
	public static Reply success()
	{
		Reply r = new Reply();
		r.data = "success".getBytes();
		return r;
	}
	

	public Reply setCookies()
	{
		Reply r = new Reply();
		r.cookies = cookies;
		return r;
	}
}
