package io.baltoro.client.test;

import javax.ws.rs.Path;

import io.baltoro.to.RequestContext;

@Path("/")
public class Class2
{

	@Path("/method2")
	public String method2(RequestContext rc)
	{
		String sessionId = rc.sessionId;
		return sessionId;
	}
	
	@Path("/hello")
	public String hello()
	{
		return "hello forom henry!";
	}
}
