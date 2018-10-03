package io.baltoro.to;

import java.util.Map;
import java.util.Set;

public class SessionUserTO
{
	public String sessionUuid;
	public String userName;
	public Set<String> roles;
	public Map<String, String> att;
	public boolean authenticated;
	public long authenticatedOn;
	public long createdOn;
	public int timeoutMin;
	
	

}
