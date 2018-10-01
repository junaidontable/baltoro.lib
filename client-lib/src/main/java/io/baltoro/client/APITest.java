package io.baltoro.client;

import java.util.Date;

import io.baltoro.features.NoAuth;
import io.baltoro.features.Path;

@Path("/")
public class APITest
{
	@Path("/helloworld")
	@NoAuth
	public String helloWorld()
	{
		return "Hello from "+Baltoro.appName+" env = "+Baltoro.env.toString()
		+"<br> sessionId = "+Baltoro.getUserSession().getSessionId()
		+"<br> timestamp  = "+new Date().toString();
	}

}
