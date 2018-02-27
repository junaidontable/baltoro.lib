package io.baltoro.client;

import java.util.HashMap;
import java.util.Map;


public class WSAPIClassInstance
{

	private static WSAPIClassInstance instances;
	private Map<String, Object> map;
	
	private WSAPIClassInstance()
	{
		map = new HashMap<>(100);		
	}
	
	static WSAPIClassInstance get()
	{
		if(instances == null)
		{
			instances = new WSAPIClassInstance();
		}
		return instances;
	}
	

	
	void add(Class<?> _class, Object obj)
	{
		map.put(_class.getName(), obj);
	}
	
	Object get(Class<?> _class)
	{
		return map.get(_class.getName());
		
	}
	

	void remove(Class<?> _class)
	{
		map.remove(_class.getName());
	}
}
