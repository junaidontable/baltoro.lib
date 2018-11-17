package io.baltoro.client;

import java.util.HashMap;
import java.util.Map;

public class CustomQuery<T>
{
	
	private Class<T> c;
	private String q;
	private Map<String, String> map = new HashMap<>(50);
	private LocalDB db;
	
	CustomQuery(Class<T> c, String q, LocalDB db)
	{
		this.c = c;
		this.q = q;
		this.db = db;
	}
	
	
	public CustomQuery<T> map(String colName, String propertyName)
	{
		map.put(colName.toLowerCase(), propertyName);
		return this;
	}
	
	
	public RecordList<T> execute()
	{
		RecordList<T> rl = db.executeQuery(c, this);
		return rl;
	}
	
	String getQuery()
	{
		return q;
	}
	
	Class<T> getClassT()
	{
		return c;
	}
	
	String getPropertyName(String colName)
	{
		return map.get(colName.toLowerCase());
	}
	
}
