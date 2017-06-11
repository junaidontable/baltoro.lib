package io.baltoro.client;

import java.util.HashMap;
import java.util.Map;

import io.baltoro.to.Principal;

public class UserSession
{

	
	private final String sessionId;
	private Map<String, Object> attMap = new HashMap<String, Object>(200);
	Principal principal;
	
	UserSession(String sessionId)
	{
		this.sessionId = sessionId;
	}
	
	public Object getObject(String attName)
	{
		return attMap.get(attName);
	}
	
	public Object addObject(String attName, Object obj)
	{
		return attMap.put(attName, obj);
	}

	public String getSessionId()
	{
		return sessionId;
	}
	
	public Principal getPrincipal()
	{
		return this.principal;
	}
}
