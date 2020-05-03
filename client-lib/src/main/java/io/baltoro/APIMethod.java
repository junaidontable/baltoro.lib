package io.baltoro;

import java.lang.reflect.Method;

public class APIMethod
{
	
	private String webPath;
	boolean authRequired = true;
	private Class<?> _class;
	private Method method;
	boolean discoverable = true;
	String propJson;
	int timeoutSec = 20;
	
	
	public APIMethod(String webPath, Class<?> _class, Method method)
	{
		this.webPath = webPath;
		this._class = _class;
		this.method = method;
	}
	

	public Class<?> get_class()
	{
		return _class;
	}

	public Method getMethod()
	{
		return method;
	}

	public String getWebPath()
	{
		return webPath;
	}
	

	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		
		if(webPath != null)
		{
			str.append(webPath+", ");
		}
			
		if(_class != null)
		{
			str.append(_class.getSimpleName()+", ");
		}
		
		if(method != null)
		{
			str.append(method.getName()+", ");
		}
		return str.toString();
	}

}
