package io.baltoro.client;

import java.util.List;

import io.baltoro.obj.Base;

public class PropertyQuery<T extends Base>
{
	
	private Class<T> c;
	private LocalDB db;
	private StringBuilder q = new StringBuilder();
	private int count;
	private String sortBy;
	
	PropertyQuery(Class<T> c, LocalDB db)
	{
		this.c = c;
		this.db = db;
		q.append("select uuid from base b where uuid in ");
		sortBy = " order by created_on desc ";
	}
	
	
	public PropertyQuery<T> addEquals(String name, String value)
	{
		if(count == 0)
		{
			q.append("\n(select distinct base_uuid from metadata where name='"+name+"' and value='"+value+"' and version_uuid=b.latest_version_uuid ");
		}
		else
		{
			q.append("\n and base_uuid in (select distinct base_uuid from metadata where name='"+name+"' and value='"+value+"' and version_uuid=b.latest_version_uuid ");
		}
		count++;
		return this;
	}
	
	public PropertyQuery<T> addLike(String name, String value)
	{
		if(count == 0)
		{
			q.append("\n(select distinct base_uuid from metadata where name='"+name+"' and value like '"+value+"' and version_uuid=b.latest_version_uuid ");
		}
		else
		{
			q.append("\n and base_uuid in (select distinct base_uuid from metadata where name='"+name+"' and value like '"+value+"' and version_uuid=b.latest_version_uuid ");
		}
		count++;
		return this;
	}
	
	public PropertyQuery<T> addIn(String name, String value)
	{
		if(count == 0)
		{
			q.append("\n(select distinct base_uuid from metadata where name='"+name+"' and value in ("+value+") and version_uuid=b.latest_version_uuid ");
		}
		else
		{
			q.append("\n and base_uuid in (select distinct base_uuid from metadata where name='"+name+"' and value in ("+value+") and version_uuid=b.latest_version_uuid ");
		}
		count++;
		return this;
	}
	
	
	public PropertyQuery<T> sortBy(String sortBy)
	{
		this.sortBy = sortBy;
		return this;
	}
	
	
	public List<T> execute()
	{
	
		for (int i = 0; i < count; i++)
		{
			q.append(")");
		}
		
		//q.append(")\n");
		
		/*
		if(count>1)
		{
			q.append(")\n");
		}
		*/
		
		String objType = db.getType(c);
		
		q.append("\nand type='"+objType+"'");
		
		if(sortBy != null)
		{
			q.append("\n"+sortBy);
		}
		
		String query = this.q.toString();
		
		System.out.println(query);
		
		RecordList<String> recList = db.query(String.class, query).execute();
	
		if(recList.isEmpty())
		{
			return null;
		}
		
		String[] uuids = recList.toArray(new String[recList.size()]);
		
		List<T> list = db.get(c, uuids);
		
		return list;
		
	}
	
	
	Class<T> getClassT()
	{
		return c;
	}
	
}

