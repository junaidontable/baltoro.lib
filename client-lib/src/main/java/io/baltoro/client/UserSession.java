package io.baltoro.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.baltoro.to.SessionUserTO;

public class UserSession
{

	//private static ObjectMapper mapper = new ObjectMapper();
	private final String sessionId;
	Map<String, String> attMap = new HashMap<String, String>(200);
	Set<String> roles = new HashSet<>();
	String userName;
	private boolean authenticated;
	
	UserSession(String sessionId)
	{
		this.sessionId = sessionId;
	}
	
	public String getAttribute(String name)
	{
		return attMap.get(name);
	}
	
	public void addAttribute(String name, String value)
	{
		attMap.put(name, value);
		sendSession();
	}
	
	public void addRoles(String roleName)
	{
		roles.add(roleName);
		sendSession();
	}


	public String getSessionId()
	{
		return sessionId;
	}

	public String getUserName()
	{
		return userName;
	}
	
	
	boolean isAuthenticated()
	{
		return authenticated;
	}

	void setAuthenticated(boolean authenticated)
	{
		this.authenticated = authenticated;
	}

	
	
	void sendSession()
	{
		SessionUserTO to = new SessionUserTO();
		to.sessionUuid = getSessionId();
		to.userName = getUserName();
		to.authenticated = authenticated;
		to.roles = roles;
		to.att = attMap;
		
		try
		{
			Baltoro.cs.validateSession(getSessionId(), to);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
