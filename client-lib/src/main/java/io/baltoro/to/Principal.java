package io.baltoro.to;

import java.util.Set;

public class Principal implements java.security.Principal
{
	private final String userName;
	private Set<String> roles;
	
	public Principal(String userName)
	{
		this.userName = userName;
	}
 
	@Override
	public String getName()
	{
		return userName;
	}
}
