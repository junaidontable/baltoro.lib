package io.baltoro.bto;

public class UserSessionContext
{
	private String sessionUuid;
	private String principalName;
	private String attJson;
	private boolean invalidateSession;
	
	
	public String getSessionUuid()
	{
		return sessionUuid;
	}
	public void setSessionUuid(String sessionUuid)
	{
		this.sessionUuid = sessionUuid;
	}
	public String getPrincipalName()
	{
		return principalName;
	}
	public void setPrincipalName(String principalName)
	{
		this.principalName = principalName;
	}
	public String getAttJson()
	{
		return attJson;
	}
	public void setAttJson(String attJson)
	{
		this.attJson = attJson;
	}
	public boolean isInvalidateSession()
	{
		return invalidateSession;
	}
	public void setInvalidateSession(boolean invalidateSession)
	{
		this.invalidateSession = invalidateSession;
	}
	
	

}
