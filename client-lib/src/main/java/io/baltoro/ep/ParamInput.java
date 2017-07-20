package io.baltoro.ep;


public interface ParamInput
{

	public EPData epData = new EPData();
	
	default public ParamInput add(String name, String value)
	{
		System.out.println();
		epData.add(name, value);
		return this;
	}
	
	
}
