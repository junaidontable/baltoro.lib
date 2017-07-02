package io.baltoro.client;

import java.util.List;

import io.baltoro.features.Endpoint;
import io.baltoro.features.Param;
import io.baltoro.to.AppTO;
import io.baltoro.to.BaseTO;
import io.baltoro.to.UserTO;

public interface AdminEP
{
	@Endpoint(appName="admin",path="/api/adminlogin")
	public UserTO adminLogin(@Param("email") String email,@Param("password") String password);
	
	
	@Endpoint(appName="admin",path="/api/app/createUser")
	public UserTO createUser(@Param("email") String email,@Param("password") String password);
	
	@Endpoint(appName="admin",path="/api/app/createApp")
	public AppTO createApp(@Param("name") String name);
	
	
	@Endpoint(appName="admin", path="/api/app/get")
	<T extends BaseTO> T getBO(@Param("base-uuid") String baseUuid) throws Exception;
	
	@Endpoint(appName="admin", path="/api/app/getMyApps", collectionReturnType=AppTO.class)
	List<AppTO> getMyApps() throws Exception;
	
}
