package io.baltoro.ep;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EPData
{

	private static ObjectMapper mapper = new ObjectMapper();
	
	List<Object[]> list = new ArrayList<Object[]>();
	
	public void add(String name, Object value)
	{
		Object[] objs = new Object[2];
		objs[0] = name;
		if(value instanceof String)
		{
			objs[1] = value;
		}
		else if(value instanceof Integer)
		{
			objs[1] = value;
		}
		else if(value instanceof Long)
		{
			objs[1] = value;
		}
		else
		{
			try
			{
				objs[1] = mapper.writeValueAsString(value);
			} 
			catch (JsonProcessingException e)
			{
				e.printStackTrace();
			}
		}
		
		list.add(objs);
	}
	
	
}
