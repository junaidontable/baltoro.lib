package io.baltoro.ep;

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
		return epData;
	}
	
	
}
