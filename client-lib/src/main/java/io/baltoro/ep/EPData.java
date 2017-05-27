package io.baltoro.ep;

import java.util.ArrayList;
import java.util.List;

public class EPData
{

	List<Object[]> list = new ArrayList<Object[]>();
	
	public void add(String name, Object value)
	{
		Object[] objs = new Object[2];
		objs[0] = name;
		objs[1] = value;
		
		list.add(objs);
	}
	
	
}
