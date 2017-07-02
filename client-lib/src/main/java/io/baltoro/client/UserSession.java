package io.baltoro.client;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.to.UserSessionContext;
import io.baltoro.to.WSTO;

public class UserSession
{

	
	private final String sessionId;
	Map<String, String> attMap = new HashMap<String, String>(200);
	Set<String> roles = new HashSet<>();
	String userName;
	
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

	public void setUserName(String userName)
	{
		//Baltoro.setUserToSession(userName);
		this.userName = userName;
		sendSession();
	}
	
	void sendSession()
	{
		WSTO to = new WSTO();
		to.appUuid = Baltoro.appUuid;
		to.instanceUuid = Baltoro.instanceUuid;
		
		UserSessionContext uctx = new UserSessionContext();
		uctx.setSessionUuid(getSessionId());
		uctx.setPrincipalName(getUserName());
		
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		byte[] toBytes = null;
		try
		{
			json = mapper.writeValueAsString(attMap);
			
			System.out.println("------------");
			System.out.println(json);
			System.out.println("------------");
			
			uctx.setAttJson(json);
			to.userSessionContext = uctx;
			toBytes = mapper.writeValueAsBytes(to);
		} 
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}
		
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(toBytes);
		WSSessions.get().addToResponseQueue(byteBuffer);
	}
}
