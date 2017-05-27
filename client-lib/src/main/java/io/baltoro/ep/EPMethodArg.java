package io.baltoro.ep;

public class EPMethodArg
{
	String type ="";
	String name;
	
	public EPMethodArg(String type, String name)
	{
		if(type != null)
			this.type = type;
		
		this.name = name;
	}
}
