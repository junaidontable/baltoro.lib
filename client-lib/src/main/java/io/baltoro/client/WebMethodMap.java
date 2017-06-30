package io.baltoro.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.baltoro.features.AbstractFilter;

public class WebMethodMap
{
	private static WebMethodMap _instance;
	private Map<String, WebMethod> pMap;
	private Map<String, Class<AbstractFilter>> filterMap;
	private List<String> filterList;
	private WebMethodMap()
	{
		filterMap = new HashMap<>();
		filterList = new ArrayList<>();
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
	
	void addFilter(int sortOrder, Class<AbstractFilter> clazz)
	{
		filterMap.put(sortOrder+clazz.toGenericString(), clazz);
		filterList.add(sortOrder+clazz.toGenericString());
		
		Collections.sort(filterList);
	}



	public List<String> getFilterNames()
	{
		return filterList;
	}
	
	public Class<AbstractFilter> getFilterClass(String key)
	{
		return filterMap.get(key);
	}
	
	
	
}
