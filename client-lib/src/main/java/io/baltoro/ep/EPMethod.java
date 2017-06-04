package io.baltoro.ep;

import java.util.ArrayList;
import java.util.List;

public class EPMethod
{
	public String returnType = "void";
	public String name;
	public List<EPMethodArg> args = new ArrayList<EPMethodArg>();
	public String appName;
	public String path;
	
	public EPMethod(String returnType, String name, String appId, String path)
	{
		if(returnType != null && returnType.length() > 0)
		{
			this.returnType = returnType;
		}
		this.name = name;
		this.appName = appId;
		this.path = path;
	}
	
	public void addArg(String argType, String argName)
	{
		args.add(new EPMethodArg(argType, argName));
	}
	
	public String getCode()
	{
		StringBuffer buffer = new StringBuffer();
		StringBuffer epData = new StringBuffer();
		
		buffer.append("	public "+returnType+" "+name+"(");
	
		boolean found=false;
		for (EPMethodArg arg : args)
		{
			found = true;
			buffer.append(arg.type+" "+arg.name+", ");
			epData.append("		data.add(\""+arg.name+"\","+arg.name+");\r\n");
		}
		if(found)
			buffer.delete(buffer.length()-2, buffer.length());
		
		buffer.append(")\n");
				
		buffer.append("	{\n");
		buffer.append("		String appName = \""+this.appName+"\";\r\n");
		buffer.append("		String path = \""+this.path+"\";\r\n");
		buffer.append("		EPData data = new EPData();\r\n");
		buffer.append(epData.toString());
		
		
		buffer.append("		CloudServer server = new CloudServer(appName);\r\n");
		//buffer.append("		Class rType = Class.forName("+returnType+");\r\n");
		buffer.append("		"+returnType+" obj = server.execute(path, data, "+returnType+".class);\r\n");
		
		
		buffer.append("		return obj;\r\n");
		buffer.append("	}\n");
		
		
		//System.out.println(buffer.toString());
		
		return buffer.toString();
	}
}
