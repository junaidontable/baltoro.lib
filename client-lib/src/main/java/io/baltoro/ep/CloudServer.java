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

import org.bouncycastle.asn1.cms.SCVPReqRes;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.Baltoro;
import io.baltoro.client.CSRequestFilter;
import io.baltoro.client.CSResponseFilter;
import io.baltoro.client.CheckRequestFilter;
import io.baltoro.client.CheckResponseFilter;
import io.baltoro.client.util.ObjectUtil;
import io.baltoro.client.util.StringUtil;
import io.baltoro.to.APIError;
import io.baltoro.to.RequestContext;

public class CloudServer
{
	
	static ExecutorService executor = Executors.newWorkStealingPool();
	static Logger log = Logger.getLogger(CloudServer.class.getName());
	static ObjectMapper mapper = new ObjectMapper();
	Client client;
	static Map<String, Client> appMap = new HashMap<>();
	String appName;
	
	boolean online = false;
	
	
	
	public CloudServer(String appName, RequestContext req)
	{
		this.appName = appName;
		
		
		CSResponseFilter responseFilter = new CSResponseFilter(appName);
		CSRequestFilter requestFilter = new CSRequestFilter(appName);
	
		
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
		
		/*
		Map<String, NewCookie> map = cookieMap.get(appName);
		
		Set<String> cookieNames = map.keySet();
		
		//log.info("sending ============= >>>>>> Cookie count ["+cookieNames.size()+"]");
		StringBuffer buffer = new StringBuffer();
		for (String cookieName : cookieNames)
		{
			NewCookie cookie = map.get(cookieName);
			//log.info("sending ============= >>>>>> ["+map.hashCode()+"]>>>>> "+cookieName+" : "+cookie);
			String _cookie = cookie.getName()+"="+cookie.getValue()+";";
			buffer.append(_cookie);
		}
		
		ib.header("Cookie", buffer.toString());
		*/
		
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
	
	
	public <T> T call(String path, EPData data, Class<T> returnType)
	{
		WebTarget target = client.target(Baltoro.getServerUrl()).path(path);	
	
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
		
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		
		String error = response.getHeaderString("BALTORO-ERROR");
		if(StringUtil.isNotNullAndNotEmpty(error))
		{
			System.out.println("////////////////////////");
			System.out.println(error);
			System.out.println("////////////////////////");
			throw new APIError(error);
		}
		
			
		String json = response.readEntity(String.class);
		if(returnType == String.class)
		{
			return returnType.cast(json);
		}
	
		Object obj = ObjectUtil.toObject(returnType, json.getBytes());
		
		return returnType.cast(obj);
	
	}
	
	public Future<?> callAsyn(String path, EPData data, Class<?> returnType)
	{
		WebTarget target = client.target(Baltoro.getServerUrl()).path(path);	
	
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
