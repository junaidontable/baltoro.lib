package io.baltoro.to;

import java.util.Map;

public class WSTO
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String uuid;
	public String appId;
	public String path;
	public Map<String, String> headers;
	public Map<String,String[]> params;
	public byte[] data;
	
}
