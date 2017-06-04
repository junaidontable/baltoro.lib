package io.baltoro.to;

import java.util.Map;

public class WSTO
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String uuid;
	public String appName;
	public String appUuid;
	public String instanceUuid;
	public String path;
	public Map<String, String> headers;
	public Map<String,String[]> requestParams;
	public byte[] data;
	public String jsonClassName;
	public RequestContext requestContext;
	
}
