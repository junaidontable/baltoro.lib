package io.baltoro.client;

import io.baltoro.bto.AppTO;
import io.baltoro.bto.BaseTO;
import io.baltoro.bto.UserTO;
import io.baltoro.features.EPReturnType;
import io.baltoro.features.Endpoint;
import io.baltoro.features.Param;

public interface AdminEP
{
	
	@Endpoint(appName="admin",path="/api/adminlogin")
	public UserTO adminLogin(@Param("email") String email,@Param("password") String password);
	
	
	@Endpoint(appName="admin",path="/api/app/createUser")
	public UserTO createUser(@Param("email") String email,@Param("password") String password);
	
	@Endpoint(appName="admin",path="/api/app/createApp")
	public AppTO createApp(@Param("name") String name);
	
	
	@Endpoint(appName="admin", path="/api/app/get")
	<T extends BaseTO> T getBO(@Param("base-uuid") String baseUuid, @EPReturnType Class<T> type) throws Exception;
	
	@Endpoint(appName="admin", path="/api/app/getMyApps")
	AppTO[] getMyApps() throws Exception;
	
}
