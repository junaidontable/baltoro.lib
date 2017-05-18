package io.baltoro.client.test;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;

import io.baltoro.client.AppId;

@RolesAllowed({"Admin","Group"})
@Path("/class1")
@AppId("12345")
public class Class1
{

	@RolesAllowed("Admin")
	public void method1()
	{
		
	}
}
