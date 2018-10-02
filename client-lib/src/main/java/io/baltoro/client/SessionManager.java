package io.baltoro.client;

import java.util.HashMap;
import java.util.Map;

class SessionManager
{

	private static Map<String, UserSession> sessionMap = new HashMap<String, UserSession>(1000);
	
	static UserSession createSession(String sessionId)
	{
		UserSession session = new UserSession(sessionId);
		sessionMap.put(sessionId, session);
		return session;
	}
	
	
	static UserSession getSession(String sessionId)
	{
		UserSession session = sessionMap.get(sessionId);
		/*
		if(session == null)
		{
			session = new UserSession(sessionId);
			sessionMap.put(sessionId, session);
		}
		*/
		return session;
	}
	
	static void removeUserSession(String sessionId)
	{
		sessionMap.remove(sessionId);
	}
	
}
