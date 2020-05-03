package io.baltoro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.baltoro.features.AbstractFilter;

public class APIMap
{
	private static APIMap _instance;
	private Map<String, APIMethod> pMap;
	private Map<String, Class<AbstractFilter>> filterMap;
	private List<String> filterList;
	private APIMap()
	{
		filterMap = new HashMap<>();
		filterList = new ArrayList<>();
	}
	
	
	
	public static APIMap getInstance()
	{
		if(_instance == null)
		{
			_instance = new APIMap();
		}
		
		return _instance;
	}
	
	public void setMap(Map<String, APIMethod> pMap)
	{
		this.pMap = pMap;
	}
	
	public APIMethod getMethod(String path)
	{
		APIMethod webMethod = pMap.get(path);
		return webMethod;
		
	}
	
	Map<String, APIMethod> getMap()
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
