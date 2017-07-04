package io.baltoro.ep;

import java.util.Arrays;
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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.Baltoro;
import io.baltoro.client.CheckRequestFilter;
import io.baltoro.client.CheckResponseFilter;
import io.baltoro.client.util.ObjectUtil;
import io.baltoro.client.util.StringUtil;
import io.baltoro.to.APIError;

public class CloudServer
{
	
	static ExecutorService executor = Executors.newWorkStealingPool();
	static Logger log = Logger.getLogger(CloudServer.class.getName());
	static ObjectMapper mapper = new ObjectMapper();
	Client client;
	String host;// = "http://127.0.0.1:8080";
	static Map<String, Map<String, NewCookie>> cookieMap = new HashMap<>();
	static Map<String, Client> cientMap = new HashMap<>();
	String appName;
	
	boolean online = false;
	
	
	
	public CloudServer(String appName)
	{
		this.appName = appName;
		
		Map<String, NewCookie> map = cookieMap.get(appName);
		if(map == null)
		{
			map = new HashMap<>(50);
			cookieMap.put(appName, map);
		}
		
		CheckResponseFilter responseFilter = new CheckResponseFilter(map);
	
		if(Baltoro.debug == true)
		{
			this.host = "http://"+appName+".baltoro.io:8080";
		}
		else
		{
			this.host = "http://"+appName+".baltoro.io";
		}
		
		client = cientMap.get(appName);
		if(client == null)
		{
			client = ClientBuilder.newBuilder()
					.register(JacksonFeature.class)
					.register(CheckRequestFilter.class)
					.register(responseFilter)
					.build();
			
			cientMap.put(appName, client);
		}
		
		
		

	
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
	
		WebTarget target = client.target(host).path("/areyouthere");	
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		
		//String sessionId = response.readEntity(String.class);
		//this.sessionCookie = new Cookie("JSESSIONID", sessionId,"/", null);
		//handleSessionCookie(response);
	}
	


	
	
	Builder getIB(WebTarget target)
	{
		Invocation.Builder ib =	target.request(MediaType.APPLICATION_JSON_TYPE);
		Map<String, NewCookie> map = cookieMap.get(appName);
		
		Set<String> cookieNames = map.keySet();
		
		for (String cookieName : cookieNames)
		{
			Cookie cookie = map.get(cookieName);
			log.info("sending ============= >>>>>>>>>>> "+cookieName+" : "+cookie);
			ib.cookie(cookie);
		}
		
		return ib;
	}
	

	public <T> T execute(String path, EPData data, Class<T> returnType, Class<?> returnSubType)
	{
		WebTarget target = client.target(host).path(path);	
	
		log.info("url --> "+target);
		
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
		
		
		String error = response.getHeaderString("BALTORO-ERROR");
		if(StringUtil.isNotNullAndNotEmpty(error))
		{
			throw new APIError(error);
		}
		//WSTO wsto = response.readEntity(WSTO.class);
		//Object obj = ObjectUtil.toObject(returnType, wsto.data);
		
			
		String json = response.readEntity(String.class);
		
		
		if(returnSubType != null)
		{
			
			Object obj = ObjectUtil.toObject(returnSubType, json.getBytes());
			return returnType.cast(obj);
		
		}
		else
		{
			Object obj = ObjectUtil.toObject(returnType, json.getBytes());
			return returnType.cast(obj);
		}
		
		/*
		try
		{
			
			JavaType type = mapper.getTypeFactory().constructArrayType(collectionReturnType);
			Object[] pojos = mapper.readValue(json, type);
			List<?> pojoList = Arrays.asList(pojos);
			return returnType.cast(pojoList);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		*/
		//return null;
		
		
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
