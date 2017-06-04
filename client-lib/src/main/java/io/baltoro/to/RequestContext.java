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
	
	
}
