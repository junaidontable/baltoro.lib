package io.baltoro.client.test;



import io.baltoro.to.RequestContext;
import io.baltoro.features.Path;

@Path("/")
//@LocalFile(localPath="/Users/junaid/Desktop/webfiles/", webPath="/")
//@LocalFile(localPath="/Users/junaid/Desktop/baltoro.lib/client-lib/target/baltoro-lib-jar-with-dependencies.jar",webPath="/downloads/client-lib.jar")
public class Class2
{

	
	@Path("/method2")
	public String method2(RequestContext rc)
	{
		String sessionId = rc.getSessionId();
		return sessionId;
	}
	
	@Path("/hello")
	public String hello()
	{
		return "hello forom henry!";
	}

}
