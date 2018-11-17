package io.baltoro.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Record
{
	private Map<String, Object> map = new HashMap<>(50);
	
	
	Record()
	{
		
	}
	
	void add(String name, Object value)
	{
		map.put(name, value);
		
	}
	
	
	public Set<String> getColmunNames()
	{
		return map.keySet();
	}
	
	
	public Object getValue(String colName)
	{
		return map.get(colName);
	}
}
