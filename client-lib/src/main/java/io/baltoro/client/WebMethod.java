package io.baltoro.client;

import java.lang.reflect.Method;

public class WebMethod
{
	
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
	
	

}
