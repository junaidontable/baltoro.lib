package io.baltoro.client;

import java.util.Date;

import io.baltoro.features.NoAuth;
import io.baltoro.features.Path;


public class APITest
{
	@Path("/helloworld")
	@NoAuth
	public String helloWorld()
	{
		return "Hello from "+Baltoro.appTO.name+" env = "+Baltoro.env
		+"<br> sessionId = "+Baltoro.getUserSession().getSessionId()
		+"<br> timestamp  = "+new Date().toString();
	}

}
