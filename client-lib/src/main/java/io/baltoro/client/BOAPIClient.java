package io.baltoro.client;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.to.MgntContext;
import io.baltoro.to.PathTO;
import io.baltoro.to.ReplicationTO;

public class BOAPIClient
{
	
	static final String POLL_SERVICE = "/PLSV93CA659B1BEB4229B49FF44852DA462F".toLowerCase();
	static final String RESP_SERVICE = "/RSSVA0BE926D318342BD9939D7AC06FE9A9B".toLowerCase();
	static final String BLTC_CLIENT = "BLCT4B0F12FA974043E3BB23D485237EAB64".toLowerCase();
	
	static Logger log = Logger.getLogger(BOAPIClient.class.getName());
	
	Client webClient;
	Client pollerClient;
	ObjectMapper mapper = new ObjectMapper();
	
	String host;
	String port;
	
	String blHost = "http://"+BLTC_CLIENT+".baltoro.io";
	//String host = "http://admin.baltoro.io";
	
	
	boolean online = false;
	
	BOAPIClient()
	{
		/*
		if(Baltoro.debug)
		{
			//blHost = "http://admin.baltoro.io:8080";
			//host = "http://admin.baltoro.io:8080";
		}
		*/
		
		
		if(Baltoro.env == Env.LOC)
		{
			blHost = "http://localhost:8080";
		}
		
		
		RequestFilter reqFilter = new RequestFilter();
		
		CheckResponseFilter responseFilter = new CheckResponseFilter("admin",Baltoro.agentCookieMap);
		
		
		webClient = ClientBuilder.newBuilder()
				.register(JacksonFeature.class)
				.register(CheckRequestFilter.class)
				.register(reqFilter)
				.register(responseFilter)
				.build();
		
		
		
		pollerClient = ClientBuilder.newBuilder()
				.register(JacksonFeature.class)
				.register(reqFilter)
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
	
	
	String createInstance(String appUuid, String serviceName, String instUuid) throws Exception
	{
		log.info("... creating new instance -> server ...");
	
		WebTarget target = webClient.target(blHost).path("/createinstance");
		
		Form form = new Form();
		form.param("appUuid", appUuid);
		form.param("serviceName", serviceName);
		form.param("inst-uud", instUuid);
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		instUuid = response.readEntity(String.class);
		return instUuid;
	}
	

	
	String getAppUuidByName(String appName) throws Exception
	{
		log.info("... getting app uuid -> server ...");
	
		WebTarget target = webClient.target(blHost).path("/getAppUuidByName");
		
		Form form = new Form();
		form.param("app-name", appName);
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		String instUuid = response.readEntity(String.class);
		return instUuid;
	}
	
	String getAppData(String appUuid) throws Exception
	{
		log.info("... getting app data -> server ...");
	
		WebTarget target = webClient.target(blHost).path("/getAppData");
		
		Form form = new Form();
		form.param("app-uuid", appUuid);
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		String data = response.readEntity(String.class);
		return data;
	}
	
	String sendAppAPI() throws Exception
	{
		log.info("... getting app data -> server ...");
		
		MgntContext ctx = new MgntContext();
		
		Map<String, WebMethod> map = WebMethodMap.getInstance().getMap();
		List<PathTO> pathList = new ArrayList<>(200);
		
		for (String key : map.keySet())
		{
			WebMethod wm = map.get(key);
			
			PathTO pto = new PathTO();
			pto.appUuid = Baltoro.appUuid;
			pto.createdBy = Baltoro.instanceUuid;
			pto.path = key;
			pto.authRequired = wm.authRequired;
			pto.discoverable = wm.discoverable;
			pto.propsJson = wm.propJson;
			pto.timeoutSec = wm.timeoutSec;
			
			pathList.add(pto);
			//System.out.println("PATH ADDING TO LIST -> "+key+" --> "+map.get(key));
		} 
		
		ctx.setPathTOs(pathList);
	
		WebTarget target = webClient.target(blHost).path("/setappapi");
		
		String json = mapper.writeValueAsString(pathList);
		
		Form form = new Form();
		form.param("app-uuid", Baltoro.appUuid);
		form.param("inst-uuid", Baltoro.instanceUuid);
		form.param("json", json);
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		String data = response.readEntity(String.class);
		return data;
	}
	
	String sendAPIResponse(String toUuid, String json) throws Exception
	{
		log.info("... getting app data -> server ...");
	
		WebTarget target = webClient.target(blHost).path(RESP_SERVICE);
			
		Form form = new Form();
		form.param("app-uuid", Baltoro.appUuid);
		form.param("inst-uuid", Baltoro.instanceUuid);
		form.param("to-uuid", toUuid);
		form.param("json", json);
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		String data = response.readEntity(String.class);
		return data;
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
	
	
	String poll(int cpu, int memoryGB)
	throws ConnectException
	{
		//log.info("... polling data  -> server ... "+Baltoro.appName+" ,,,, "+Baltoro.serviceNames.toString());
	
	
		WebTarget target = pollerClient.target(blHost)
				.path(POLL_SERVICE)
				.queryParam("BLT_CPU", cpu)
				.queryParam("BLT_MEMORY_GB", memoryGB);
	
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		String json = response.readEntity(String.class);
		//log.info("response ==>"+json);
		
		
		return json;
	}
	
	

}
