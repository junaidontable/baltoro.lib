package io.baltoro.client;

import java.lang.reflect.Method;

public class WebMethod
{
	
	String webPath;
	String localFilePath;
	
	private Class<?> _class;
	public Class<?> get_class()
	{
		return _class;
	}

	public Method getMethod()
	{
		return method;
	}

	private Method method;
	
	public WebMethod(Class<?> _class, Method method)
	{
		this._class = _class;
		this.method = method;
	}
	
	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		
		if(webPath != null)
		{
			str.append(webPath+", ");
		}
		
		if(localFilePath != null)
		{
			str.append(localFilePath+", ");
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
