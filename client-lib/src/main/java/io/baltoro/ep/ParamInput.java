package io.baltoro.ep;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface ParamInput
{

	EPData epData = new EPData();
	
	ParamInput get(ParamInput input);
	
	
	default ParamInput add(String name, String value)
	{
		epData.add(name, value);
		return this;
	}
	
	default EPData getEPData()
	{
		get(this);
		List<Object[]> list = epData.list;
		EPData data = new EPData();
		data.list = list;
		epData.list = new ArrayList<>();
		return data;
	}
	
	
}
