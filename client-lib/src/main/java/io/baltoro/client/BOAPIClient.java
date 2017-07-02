package io.baltoro.client;

import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;

public class BOAPIClient
{
	
	static Logger log = Logger.getLogger(BOAPIClient.class.getName());
	
	Client webClient;
	String blHost = "http://admin.baltoro.io";
	String host = "http://admin.baltoro.io";
	
	Baltoro baltoro;
	boolean online = false;
	
	BOAPIClient(Baltoro baltoro)
	{
		if(Baltoro.debug)
		{
			blHost = "http://admin.baltoro.io:8080";
			host = "http://admin.baltoro.io:8080";
		}
		
		CheckResponseFilter responseFilter = new CheckResponseFilter(baltoro.agentCookieMap);
		
		webClient = ClientBuilder.newBuilder()
				.register(JacksonFeature.class)
				.register(CheckRequestFilter.class)
				.register(responseFilter)
				.build();
		
		this.baltoro = baltoro;
	
		try
		{
			areYouThere();
			online = true;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			online = false;
		}
	}
	
	
	void areYouThere() throws Exception
	{
		log.info("... Are you There ...");
	
		WebTarget target = webClient.target(blHost).path("/areyouthere");	
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		String str = response.readEntity(String.class);
		log.info("response ==>"+str);
	}
	
	int getRemainingInsanceThreadsCount(String appName, String instanceUuid) throws Exception
	{
		log.info("... getInsanceThreadsCount ...");
	
		WebTarget target = webClient.target(blHost).path("/getRemainingInsanceThreadsCount");
		
		Form form = new Form();
		form.param("appName", appName);
		form.param("instanceUuid", instanceUuid);
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		String count = response.readEntity(String.class);
		return Integer.parseInt(count);
	}
	
	Builder getIB(WebTarget target)
	{
		Invocation.Builder ib =	target.request(MediaType.APPLICATION_JSON_TYPE);
		Set<String> cookieNames = baltoro.agentCookieMap.keySet();
		
		for (String cookieName : cookieNames)
		{
			Cookie cookie = baltoro.agentCookieMap.get(cookieName);
			log.info("sending ============= >>>>>>>>>>> "+cookieName+" : "+cookie);
			ib.cookie(cookie);
		}	
		return ib;
	}
	
	/*
	UserTO login(String email, String password) throws Exception
	{
		WebTarget target = webClient.target(host).path("/api/adminlogin");	
	
		Form form = new Form();
		form.param("email", email);
		form.param("password", password);
		
		Invocation.Builder ib =	getIB(target);
		
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		String json = response.readEntity(String.class);
		
		System.out.println(json);
		
		UserTO to = response.readEntity(UserTO.class);
	
		UserSession userSession = Baltoro.getUserSession();
		userSession.setUserName(to.uuid);
		
			
		return to;
	}
	

	
	
	ContainerTO createContainer() throws Exception
	{
		//log.info("... create container ...");
		
		WebTarget target = webClient.target(host).path("/api/app/createContainer");
		 
		Form form = new Form();
		form.param("name", "customer 1");
		
		Invocation.Builder ib =	getIB(target);
		
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		ContainerTO container = response.readEntity(ContainerTO.class);
		
		//log.info("container : ... "+container.getBaseUuid());
		
		return container;
		
	}
	

	
	<T extends BaseTO> T getBO(String baseUuid, Class<T> type) throws Exception
	{
	
		String url = "/api/app/get";
	
		WebTarget target = webClient.target(host).path(url);
		target = target.queryParam("base-uuid", baseUuid);
		 
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		BaseTO bo = response.readEntity(type);
		
		return type.cast(bo);
		
	}
	
	
	
	UserTO createUser(String email, String password) throws Exception
	{
		
		WebTarget target = webClient.target(host).path("/api/app/createUser");
		 
		Form form = new Form();
		form.param("email", email);
		form.param("password", password);
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		UserTO user = response.readEntity(UserTO.class);
		return user;
		
	}
	
	AppTO createApp(String name) throws Exception
	{
		
		WebTarget target = webClient.target(host).path("/api/app/createApp");
		 
		Form form = new Form();
		form.param("name", name);
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		AppTO app = response.readEntity(AppTO.class);
		return app;
		
	}
	
	List<AppTO> getMyApps() throws Exception
	{
		WebTarget target = webClient.target(host).path("/api/app/getMyApps");
		Form form = new Form();
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		List<AppTO> list = response.readEntity(new GenericType<List<AppTO>>(){});
		return list;
		
	}
	*/

	

	

}
