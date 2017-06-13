package io.baltoro.client.test;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;

@RolesAllowed({"Admin","Group"})
@Path("/class1")
public class Class1
{
	
	@Path("/method1")
	public void method1()
	{
		
	}

}
