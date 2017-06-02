package io.baltoro.client;

import java.util.List;
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
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;

import io.baltoro.to.AppTO;
import io.baltoro.to.BaseTO;
import io.baltoro.to.ContainerTO;
import io.baltoro.to.UserTO;

public class BOAPIClient
{
	
	static Logger log = Logger.getLogger(BOAPIClient.class.getName());
	
	Client webClient;
	String host = "http://api.baltoro.io";
	
	Baltoro baltoro;
	boolean online = false;
	
	BOAPIClient(Baltoro baltoro)
	{
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
	
		WebTarget target = webClient.target(host).path("/baltoro/api/areyouthere");	
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		log.info("sessionId ==>"+response);
	}
	
	
	
	UserTO login(String email, String password) throws Exception
	{
		WebTarget target = webClient.target(host).path("/baltoro/api/auth/login");	
	
		Form form = new Form();
		form.param("email", email);
		form.param("password", password);
		
		Invocation.Builder ib =	getIB(target);
		
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		UserTO user = response.readEntity(UserTO.class);
			
		return user;
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
	
	
	ContainerTO createContainer() throws Exception
	{
		//log.info("... create container ...");
		
		WebTarget target = webClient.target(host).path("/baltoro/api/bo/createContainer");
		 
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
	
		String url = "/baltoro/api/bo/";
		if(type == ContainerTO.class)
		{
			url = url+"getContainer";
		}
		else if(type == UserTO.class)
		{
			url = url+"getUser";
		}
		else if(type == AppTO.class)
		{
			url = url+"getApp";
		}
		
		
		WebTarget target = webClient.target(host).path(url);
		target = target.queryParam("uuid", baseUuid);
		 
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		BaseTO bo = response.readEntity(type);
		
		return type.cast(bo);
		
	}
	
	
	
	UserTO createUser(String email, String password) throws Exception
	{
		
		WebTarget target = webClient.target(host).path("/baltoro/api/bo/createUser");
		 
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
		
		WebTarget target = webClient.target(host).path("/baltoro/api/bo/createApp");
		 
		Form form = new Form();
		form.param("name", name);
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		AppTO app = response.readEntity(AppTO.class);
		return app;
		
	}
	
	List<AppTO> getMyApps() throws Exception
	{
		WebTarget target = webClient.target(host).path("/baltoro/api/bo/getMyApps");
		Form form = new Form();
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		List<AppTO> list = response.readEntity(new GenericType<List<AppTO>>(){});
		return list;
		
	}
	

	

	

}
