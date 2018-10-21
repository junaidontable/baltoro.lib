package io.baltoro.ep;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

@FunctionalInterface
public interface ParamInput
{

	EPData epData = new EPData();
	
	ParamInput get(ParamInput input);
	
	
	default ParamInput add(String name, String value)
	{
		if(value == null)
		{
			value = "";
		}
		epData.add(name, value);
		return this;
	}
	
	default ParamInput add(String name, Optional<String> value)
	{
		String _value = null;
		if(value == null || value.get() == null)
		{
			_value = "";
		}
		else
		{
			_value = value.get();
		}
		
		
		epData.add(name, _value);
		return this;
	}
	
	default ParamInput add(String name, Object value)
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
