package io.baltoro.ep;

import java.util.ArrayList;
import java.util.List;

public class EPMethod
{
	public String returnObj = "void";
	public String name;
	public List<EPMethodArg> args = new ArrayList<EPMethodArg>();
	public String appName;
	public String path;
	
	
	public EPMethod(String returnObj, String name, String appId, String path)
	{
		if(returnObj != null && returnObj.length() > 0)
		{
			this.returnObj = returnObj;
		}
		this.name = name;
		this.appName = appId;
		this.path = path;
	}
	
	
	public void addArg(String argType, String argName, boolean epReturnType)
	{
		args.add(new EPMethodArg(argType, argName, epReturnType));
	}
	
	public String getCode()
	{
		StringBuffer buffer = new StringBuffer();
		StringBuffer epData = new StringBuffer();
		
		String retunSubType = null;
		buffer.append("	public "+returnObj+" "+name+"(");
	
		boolean found=false;
		int count = 0;
		for (EPMethodArg arg : args)
		{
			count++;
			found = true;
			buffer.append(arg.type+" arg"+ count +", ");
			
			if(!arg.epReturnType)
			{ 
				epData.append("		data.add(\""+arg.name+"\", arg"+count+");\r\n");
			}
			else
			{
				retunSubType = "arg"+count;
			}
			
			
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
	
		if(retunSubType == null)
		{
			buffer.append("		"+returnObj+" obj = server.execute(path, data, "+returnObj+".class, null);\r\n");
		}
		else
		{
			buffer.append("		"+returnObj+" obj = server.execute(path, data, "+returnObj+".class, "+retunSubType+");\r\n");
		}
		
		
		buffer.append("		return obj;\r\n");
		buffer.append("	}\n");
		
		
		System.out.println(buffer.toString());
		
		return buffer.toString();
	}
}
