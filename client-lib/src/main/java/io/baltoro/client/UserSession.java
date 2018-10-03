package io.baltoro.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.baltoro.to.SessionUserTO;

public class UserSession
{


	private final String sessionId;
	Map<String, String> attMap = new HashMap<String, String>(200);
	Set<String> roles = new HashSet<>();
	private String userName;
	private boolean authenticated;
	private long createdOn;
	private long authenticatedOn;
	private int timeoutMin = 20;
	
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

	
	void setUserName(String userName)
	{
		this.userName = userName;
	}

	boolean isAuthenticated()
	{
		return authenticated;
	}

	void setAuthenticated(boolean authenticated)
	{
		this.authenticated = authenticated;
		this.authenticatedOn = System.currentTimeMillis();
	}

	
	
	public long getCreatedOn()
	{
		return createdOn;
	}

	void setCreatedOn(long createdOn)
	{
		this.createdOn = createdOn;
	}
	
	

	public int getTimeoutMin()
	{
		return timeoutMin;
	}

	void setTimeoutMin(int timeoutMin)
	{
		this.timeoutMin = timeoutMin;
	}

	void sendSession()
	{
		SessionUserTO to = new SessionUserTO();
		to.sessionUuid = getSessionId();
		to.userName = getUserName();
		to.authenticated = authenticated;
		to.roles = roles;
		to.att = attMap;
		to.createdOn = createdOn;
		to.authenticatedOn = authenticatedOn;
		to.timeoutMin = timeoutMin;
		
		try
		{
			if(isAuthenticated())
			{
				Baltoro.cs.validateSession(getSessionId(), to);
			}
			else
			{
				Baltoro.cs.inValidateSession(getSessionId());
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
