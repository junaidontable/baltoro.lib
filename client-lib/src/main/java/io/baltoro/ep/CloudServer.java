package io.baltoro.ep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;

import io.baltoro.client.CheckRequestFilter;
import io.baltoro.client.CheckResponseFilter;
import io.baltoro.client.util.ObjectUtil;
import io.baltoro.client.util.StringUtil;

public class CloudServer
{
	
	static ExecutorService executor = Executors.newWorkStealingPool();
	static Logger log = Logger.getLogger(CloudServer.class.getName());
	
	Client client;
	String host;// = "http://127.0.0.1:8080";
	Map<String, NewCookie> cookieMap = new HashMap<String, NewCookie>(100);
	
	boolean online = false;
	
	
	
	public CloudServer(String appName)
	{
		CheckResponseFilter responseFilter = new CheckResponseFilter(cookieMap);
	
		this.host = "http://"+appName+".baltoro.io/app";
		client = ClientBuilder.newBuilder()
				.register(JacksonFeature.class)
				.register(CheckRequestFilter.class)
				.register(responseFilter)
				.build();
		

	
		try
		{
			//areYouThere();
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
	
		WebTarget target = client.target(host).path("/api/areyouthere");	
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		handleCookie(response);
		//String sessionId = response.readEntity(String.class);
		//this.sessionCookie = new Cookie("JSESSIONID", sessionId,"/", null);
		//handleSessionCookie(response);
	}
	

	void handleCookie(Response response)
	{
		Map<String, NewCookie> map = response.getCookies();
		for (String key : map.keySet())
		{
			NewCookie cookie = map.get(key);
			log.info("received ============= >>>>>>>>>>> "+key+" : "+cookie);
			cookieMap.put(key, cookie);
		}	
	}

	
	
	Builder getIB(WebTarget target)
	{
		Invocation.Builder ib =	target.request(MediaType.APPLICATION_JSON_TYPE);
		Set<String> cookieNames = cookieMap.keySet();
		for (String cookieName : cookieNames)
		{
			Cookie cookie = cookieMap.get(cookieName);
			log.info("sending ============= >>>>>>>>>>> "+cookieName+" : "+cookie);
			ib.cookie(cookie);
		}
		
		return ib;
	}
	

	public <T> T execute(String path, EPData data, Class<T> returnType)
	{
		WebTarget target = client.target(host).path(path);	
	
		//log.info("url --> "+target);
		
		Form form = new Form();
		
		List<Object[]> list = data.list;
		for (Object[] objects : list)
		{
			String name = (String) objects[0];
			String value = (String) objects[1];
			form.param(name, value);
		}
		
		Invocation.Builder ib =	getIB(target);
		
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		handleCookie(response);
		
		String error = response.getHeaderString("BALTORO-ERROR");
		if(StringUtil.isNotNullAndNotEmpty(error))
		{
			return (T)error;
		}
		//WSTO wsto = response.readEntity(WSTO.class);
		//Object obj = ObjectUtil.toObject(returnType, wsto.data);
		String str = response.readEntity(String.class);
		
		Object obj = ObjectUtil.toObject(returnType, str.getBytes());
				
		return (T)obj;
	}
	
	public Response execute(Form form, WebTarget target)
	{
		
		Callable<Response> apiCall = () ->
		{
			Invocation.Builder ib =	getIB(target);
			Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
			return response;
			
		};
		
		Future<Response> future = executor.submit(apiCall);
		
		return null;
	}

}
