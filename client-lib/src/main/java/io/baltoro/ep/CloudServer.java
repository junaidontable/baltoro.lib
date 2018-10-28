package io.baltoro.ep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.Baltoro;
import io.baltoro.client.CSRequestFilter;
import io.baltoro.client.CSResponseFilter;
import io.baltoro.client.UserSession;
import io.baltoro.client.util.ObjectUtil;
import io.baltoro.client.util.StringUtil;
import io.baltoro.to.APIError;

public class CloudServer
{
	
	static ExecutorService executor = Executors.newWorkStealingPool();
	static Logger log = Logger.getLogger(CloudServer.class.getName());
	static ObjectMapper mapper = new ObjectMapper();
	Client client;
	static Map<String, Client> appMap = new HashMap<>();
	String appName;
	
	boolean online = false;
	
	
	
	public CloudServer(String appName, UserSession session)
	{
		this.appName = appName;
		
		
		CSResponseFilter responseFilter = new CSResponseFilter(appName, session);
		CSRequestFilter requestFilter = new CSRequestFilter(appName, session);
	
		
		client = appMap.get(appName);
		if(client == null)
		{
			client = ClientBuilder.newBuilder()
					.register(JacksonFeature.class)
					.register(requestFilter)
					.register(responseFilter)
					.build();
			
			appMap.put(appName, client);
		}
		
		
	}
	

	
	
	Builder getIB(WebTarget target)
	{
	
		Invocation.Builder ib =	target.request(MediaType.APPLICATION_JSON_TYPE);
		return ib;
	}
	

	public <T> T execute(String path, EPData data, Class<T> returnType, Class<?> returnSubType)
	{
		WebTarget target = client.target(Baltoro.getServerUrl()).path(path);	
	
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
			System.out.println("////////////////////////");
			System.out.println(error);
			System.out.println("////////////////////////");
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
	
	
	public <T> T call(String serverUrl, String path, EPData data, Class<T> returnType)
	{
		WebTarget target = client.target(serverUrl).path(path);	
	
		log.info("API call URL --> "+serverUrl+path);
		
		Form form = new Form();
		
		if(data != null)
		{
			List<Object[]> list = data.list;
			for (Object[] objects : list)
			{
				String name = (String) objects[0];
				String value = (String) objects[1];
				form.param(name, value);
			}
		}
		
		form.param("appName", "flocap-envdv");
		
		Invocation.Builder ib =	getIB(target);
		
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		
		String error = response.getHeaderString("BALTORO-ERROR");
		if(StringUtil.isNotNullAndNotEmpty(error))
		{
	
			System.out.println("////////////////////////");
			System.out.println(error);
			System.out.println("////////////////////////");
			if(error.startsWith("BLT-OBJ:NOT-FOUND"))
			{
				return null;
			}
			throw new APIError(error);
		}
		
		if (returnType == null)
		{
			return null;
		}
		
		String json = response.readEntity(String.class);
		if(returnType == String.class)
		{
			return returnType.cast(json);
		}
	
		
		Object obj = ObjectUtil.toObject(returnType, json.getBytes());
		
		return returnType.cast(obj);
	
	}
	
	public Future<?> callAsyn(String url, String path, EPData data, Class<?> returnType)
	{
		WebTarget target = client.target(url).path(path);	
	
		log.info("url --> "+target);
		
		Form form = new Form();
		
		if(data != null)
		{
			List<Object[]> list = data.list;
			for (Object[] objects : list)
			{
				String name = (String) objects[0];
				String value = (String) objects[1];
				form.param(name, value);
			}
		}
		
		
		Invocation.Builder ib =	getIB(target);
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<?> future = executor.submit(() -> 
			{
				
				
				Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
				
				
				String error = response.getHeaderString("BALTORO-ERROR");
				if(StringUtil.isNotNullAndNotEmpty(error))
				{
					System.out.println("////////////////////////");
					System.out.println(error);
					System.out.println("////////////////////////");
					return error;
				}
				
					
				String json = response.readEntity(String.class);
				if(returnType == String.class)
				{
					return returnType.cast(json);
				}
			
				Object obj = ObjectUtil.toObject(returnType, json.getBytes());
				return returnType.cast(obj);
				
			});
		
		return future;
	
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
