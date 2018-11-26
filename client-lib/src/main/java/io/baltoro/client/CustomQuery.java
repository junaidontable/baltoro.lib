package io.baltoro.client;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import io.baltoro.client.util.StringUtil;

public class CustomQuery<T>
{
	
	private Class<T> c;
	private String q;
	private Map<String, String> map = new HashMap<>(50);
	private LocalDB db;
	private RecordList<T> rl;
	private boolean executed;
	
	CustomQuery(Class<T> c, String q, LocalDB db)
	{
		this.c = c;
		this.q = q;
		this.db = db;
		
		if(c == String.class)
		{
			map.put("1", "");
		}
	}
	
	
	public CustomQuery<T> map(String colName, String propertyName)
	{
		map.put(colName.toLowerCase(), propertyName);
		return this;
	}
	
	public CustomQuery<T> map(String colName, Field f)
	{
		map.put(colName.toLowerCase(), f.getName());
		return this;
	}
	
	
	public RecordList<T> execute()
	{
		if(!executed)
		{
			rl = db.executeQuery(c, this);
		}
		executed = true;
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
	
	public CustomQuery<T> displayHeaders()
	{
		execute();
		
		for (ColumnMetadata md : rl.getColMD())
		{
			int len =  md.getMaxLen() > md.getColName().length() ? md.getMaxLen() : md.getColName().length();
			System.out.print(StringUtil.pad(md.getColName(), len, '*'));
			System.out.print(" | ");
		}
		
		System.out.println("");
		
		return this;
	}
	
	public void displayResults()
	{
		execute();
		
		for (T t : rl)
		{
			rl.getColMD().forEach(md -> 
			{
			
				Object v = null;
				if(t instanceof Record)
				{
					v = ((Record)t).getValue(md.getColName());
				}
				else
				{
					String pName = getPropertyName(md.getColName());
					if(pName != null)
					{
						try
						{
							v = BeanUtils.getProperty(t, pName);
						} 
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				
				
				int len =  md.getMaxLen() > md.getColName().length() ? md.getMaxLen() : md.getColName().length();
				System.out.print(StringUtil.pad(v.toString(),len, ' '));
				System.out.print(" | ");
				
			});
			
			System.out.println("");
		}
	
		
		
	}
}
