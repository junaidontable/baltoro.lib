package io.baltoro.ep;

import javax.ws.rs.FormParam;

import io.baltoro.features.Endpoint;

public interface TestEndpointCall1
{
	@Endpoint(appName="junaid",path="/hello")
	public  String hello();
	
	@Endpoint(appName="junaid",path="/test")
	public  String test(@FormParam("name") String name,@FormParam("count") int count);
}
