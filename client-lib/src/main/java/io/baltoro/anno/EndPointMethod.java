package io.baltoro.anno;

import java.util.ArrayList;
import java.util.List;

public class EndPointMethod
{
	public String returnType = "void";
	public String name;
	public List<EndPointMethodArg> args = new ArrayList<EndPointMethodArg>();
	
	public EndPointMethod(String returnType, String name)
	{
		if(returnType != null && returnType.length() > 0)
		{
			this.returnType = returnType;
		}
		this.name = name;
	}
	
	public void addArg(String argType, String argName)
	{
		args.add(new EndPointMethodArg(argType, argName));
	}
	
	public String getCode()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("	public "+returnType+" "+name+"(");
	
		boolean found=false;
		for (EndPointMethodArg arg : args)
		{
			found = true;
			buffer.append(arg.type+" "+arg.name+", ");
		}
		if(found)
			buffer.delete(buffer.length()-2, buffer.length());
		
		buffer.append(")\n");
				
		buffer.append("	{\n");
		buffer.append("		return \"Hi!\";\n");
		buffer.append("	}\n");
		
		return buffer.toString();
	}
}
