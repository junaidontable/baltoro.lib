package io.baltoro.anno;

public class EndPointMethodArg
{
	String type ="";
	String name;
	
	public EndPointMethodArg(String type, String name)
	{
		if(type != null)
			this.type = type;
		
		this.name = name;
	}
}
