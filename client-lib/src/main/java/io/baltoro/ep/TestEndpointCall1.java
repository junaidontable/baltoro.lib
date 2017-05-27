package io.baltoro.ep;

import javax.ws.rs.FormParam;

import io.baltoro.ep.EndPoint;

public interface TestEndpointCall1
{
	@EndPoint(appId="junaid",path="/hello")
	public  String hello();
	
	@EndPoint(appId="junaid",path="/test")
	public  String test(@FormParam("name") String name,@FormParam("count") int count);
}
