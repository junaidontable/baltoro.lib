package io.baltoro.ep;

public class EPMethodArg
{
	String type ="";
	String name;
	boolean epReturnType;
	
	
	public EPMethodArg(String type, String name, boolean epReturnType)
	{
		if(type != null)
			this.type = type;
		
		this.name = name;
		this.epReturnType = epReturnType;
	}
	

}
