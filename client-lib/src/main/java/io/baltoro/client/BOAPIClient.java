package io.baltoro.client;

import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;

import io.baltoro.to.ReplicationTO;

public class BOAPIClient
{
	
	static Logger log = Logger.getLogger(BOAPIClient.class.getName());
	
	Client webClient;
	String blHost = "http://admin.baltoro.io";
	String host = "http://admin.baltoro.io";
	
	
	boolean online = false;
	
	BOAPIClient()
	{
		if(Baltoro.debug)
		{
			blHost = "http://admin.baltoro.io:8080";
			host = "http://admin.baltoro.io:8080";
		}
		
		CheckResponseFilter responseFilter = new CheckResponseFilter("admin",Baltoro.agentCookieMap);
		
		webClient = ClientBuilder.newBuilder()
				.register(JacksonFeature.class)
				.register(CheckRequestFilter.class)
				.register(responseFilter)
				.build();
		
	
		try
		{
			areYouThere();
			online = true;
		} 
		catch (Exception e)
		{
			
			e.printStackTrace();
			
			log.warning(" --------------------------------------------------");
			log.warning(" --------------------------------------------------");
			log.warning("IF RUNNING LOCAL TURN DEBUG FLAG FOR 8080 PORT !!!!");
			log.warning(" ------ -Dbaltoro.debug=true ---------------------");
			log.warning(" --------------------------------------------------");
			log.warning("EXITING CURRENT APP");
			
			online = false;
			System.exit(1);
		}
	}
	
	
	void areYouThere() throws Exception
	{
		log.info("... Are you There ..."+blHost);
	
		WebTarget target = webClient.target(blHost).path("/areyouthere");	
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		String str = response.readEntity(String.class);
		log.info("response ==>"+str);
	}
	
	int getRemainingInsanceThreadsCount(String appUuid, String instanceUuid) throws Exception
	{
		log.info("... getInsanceThreadsCount ...");
	
		WebTarget target = webClient.target(blHost).path("/getRemainingInsanceThreadsCount");
		
		Form form = new Form();
		form.param("appUuid", appUuid);
		form.param("instanceUuid", instanceUuid);
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		String count = response.readEntity(String.class);
		
		System.out.println(" ----> count "+count);
		return Integer.parseInt(count);
	}
	
	
	String createInstance(String appUuid) throws Exception
	{
		log.info("... creating new instance -> server ...");
	
		WebTarget target = webClient.target(blHost).path("/createinstance");
		
		Form form = new Form();
		form.param("appUuid", appUuid);
		form.param("clusterPath", Baltoro.clusterPath);
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		String instUuid = response.readEntity(String.class);
		return instUuid;
	}
	
	
	Builder getIB(WebTarget target)
	{
		Invocation.Builder ib =	target.request(MediaType.APPLICATION_JSON_TYPE);
		Set<String> cookieNames = Baltoro.agentCookieMap.keySet();
		
		StringBuffer buffer = new StringBuffer();
		for (String cookieName : cookieNames)
		{
			NewCookie cookie = Baltoro.agentCookieMap.get(cookieName);
			//log.info("sending ============= >>>>>> ["+Baltoro.agentCookieMap.hashCode()+"]>>>>> "+cookieName+" : "+cookie);
			String _cookie = cookie.getName()+"="+cookie.getValue()+";";
			buffer.append(_cookie);
		}
		
		ib.header("Cookie", buffer.toString());
	
		return ib;
	}
	
	
	
	ReplicationTO getReplication(String appUuid, String instUuid, String lcpUuid, long lcpMillis, boolean reset) throws Exception
	{
		log.info("... creating new instance -> server ...");
	
		WebTarget target = webClient.target(blHost).path("/getreplication");
		
		Form form = new Form();
		form.param("appUuid", appUuid);
		form.param("instUuid", instUuid);
		form.param("lcpUuid", lcpUuid);
		form.param("lcpMillis", ""+lcpMillis);
		form.param("reset", reset == true ? "true":"false");
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		ReplicationTO to = response.readEntity(ReplicationTO.class);
		return to;
	}
	
	

}
