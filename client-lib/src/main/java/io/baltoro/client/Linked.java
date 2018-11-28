package io.baltoro.client;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.baltoro.client.LocalDB.Direction;
import io.baltoro.client.util.StringUtil;
import io.baltoro.obj.Base;


public class Linked<T extends Base>
{
	
	private String srcUuid;
	private Direction direction;
	private int count;
	private List<String> uuids;
	private List<T> objs;
	private Map<String, Set<String>> attUuidMap;
	private Map<String, Set<Base>> attObjMap;
	
	private Class<T> c;
	private LocalDB db;
	
	
	Linked(Class<T> c, String srcUuid, Direction direction, LocalDB db)
	{
		this.c = c;
		this.db = db;
		this.srcUuid = srcUuid;
		this.direction = direction;
	}
	
	public List<String> getUuids()
	{
		return uuids;
	}
	
	void setUuids(List<String> uuids)
	{
		this.uuids = uuids;
	}

	public int getCount()
	{
		return count;
	}

	void setCount(int count)
	{
		this.count = count;
	}

	public List<T> getList()
	{
		return objs;
	}

	void setList(List<T> list)
	{
		this.objs = list;
	}
	
	public String getFirstUuid()
	{
		return uuids.get(0);
	}
	
	public T getFirst()
	{
		getAll();
		return objs.get(0);
	}
	
	public List<T> getAll()
	{
		if(StringUtil.isNullOrEmpty(uuids))
		{
			return new ArrayList<>();
		}
		if(objs == null)
		{
			List<T> objs = (List<T>) db.get(uuids);
			setList(objs);
		}
		return objs;
	}
	
	public T[] getAllAsArray()
	{
		getAll();
		
		Object[] arr = (Object[]) Array.newInstance(c, objs.size());
		return (T[]) objs.toArray(arr);
	}
	
	public String[] getUuidsAsArray()
	{
		return uuids.toArray(new String[uuids.size()]);
	}
	
	
	public Class<T> getType()
	{
		return c;
	}
	
	
	public Set<String> getAttUuids(String objUuid)
	{
		if(attUuidMap == null)
		{
			attUuidMap = db.getLinkAtt(srcUuid, uuids, direction);
		}
		
		Set<String> set = attUuidMap.get(objUuid);
		
		return set;
	}
	
	/*
	public Set<Base> getAttObjs(String objUuid)
	{
		getAttUuids(objUuid);
		
		if(attObjMap == null)
		{
			Set<String> allAttUuids = new HashSet<>(500);
			for (Set<String> attUuid : attUuidMap.values())
			{
				for (String val : attUuid)
				{
					allAttUuids.add(val);
				}
			}
			attObjMap = db.get(allAttUuids);
		}
		
	}
	*/
	
}
