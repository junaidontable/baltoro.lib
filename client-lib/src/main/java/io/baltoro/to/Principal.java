package io.baltoro.to;

import java.util.HashSet;
import java.util.Set;

public class Principal implements java.security.Principal
{
	private final String userName;
	private Set<String> roles = new HashSet<>();
	
	public Principal(String userName)
	{
		this.userName = userName;
	}
 
	@Override
	public String getName()
	{
		return userName;
	}
	
	public Set<String> getRoles()
	{
		return roles;
	}
	
	public void addRole(String roleName)
	{
		roles.add(roleName);
	}
}
