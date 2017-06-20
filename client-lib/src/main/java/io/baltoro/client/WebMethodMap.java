package io.baltoro.client;

import java.util.Map;

public class WebMethodMap
{
	private static WebMethodMap _instance;
	private Map<String, WebMethod> pMap;

	private WebMethodMap()
	{
		// TODO Auto-generated constructor stub
	}
	
	
	
	public static WebMethodMap getInstance()
	{
		if(_instance == null)
		{
			_instance = new WebMethodMap();
		}
		
		return _instance;
	}
	
	public void setMap(Map<String, WebMethod> pMap)
	{
		this.pMap = pMap;
	}
	
	public WebMethod getMethod(String path)
	{
		WebMethod webMethod = pMap.get(path);
		return webMethod;
		
	}
	
	Map<String, WebMethod> getMap()
	{
		return pMap;
	}
	
}
