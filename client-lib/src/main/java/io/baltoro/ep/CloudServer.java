package io.baltoro.ep;

import java.util.List;
import java.util.Map;
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
import io.baltoro.util.ObjectUtil;
import io.baltoro.util.StringUtil;

public class CloudServer
{
	
	static Logger log = Logger.getLogger(CloudServer.class.getName());
	
	Client client;
	Cookie sessionCookie;
	//String host = "http://api.baltoro.org:8080";
	String host;// = "http://127.0.0.1:8080";
	
	
	boolean online = false;
	
	
	
	public CloudServer(String appId)
	{
		this.host = "http://"+appId+".baltoro.io/baltoro/app";
		client = ClientBuilder.newBuilder()
				.register(JacksonFeature.class)
				.register(CheckRequestFilter.class)
				.register(CheckResponseFilter.class)
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
	
		WebTarget target = client.target(host).path("/baltoro/api/areyouthere");	
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		String sessionId = response.readEntity(String.class);
		this.sessionCookie = new Cookie("JSESSIONID", sessionId,"/", null);
		//handleSessionCookie(response);
	}
	

	void handleSessionCookie(Response response) throws Exception
	{
		Map<String, NewCookie> map = response.getCookies();
		for (String key : map.keySet())
		{
			NewCookie cookie = map.get(key);
			log.info(key+" : "+cookie);
			if(key.equals("JSESSIONID"))
			{
				String domain = cookie.getDomain();
				sessionCookie = new Cookie(cookie.getName(), cookie.getValue(),cookie.getPath(), domain);
			}
		}	
	}

	
	
	Builder getIB(WebTarget target)
	{
		Invocation.Builder ib =	target.request(MediaType.APPLICATION_JSON_TYPE);
		if(sessionCookie != null)
		{
			ib.cookie(sessionCookie); 
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
}
