package io.baltoro.client.test;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;

import io.baltoro.client.CTX;
import io.baltoro.client.UserSession;

@RolesAllowed({"Admin","Group"})
@Path("/class1")
public class Class1
{
	
	@CTX UserSession userSession;

	@Path("/method1")
	public void method1()
	{
		
	}

}
