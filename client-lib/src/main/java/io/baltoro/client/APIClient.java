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

import io.baltoro.client.LocalDB.Repl;
import io.baltoro.client.util.StringUtil;
import io.baltoro.to.PathTO;
import io.baltoro.to.ReplicationTO;
import io.baltoro.to.SessionUserTO;

public class APIClient
{
	
	static final String POLL_SERVICE = "/PLSV93CA659B1BEB4229B49FF44852DA462F".toLowerCase();
	static final String RESP_SERVICE = "/RSSVA0BE926D318342BD9939D7AC06FE9A9B".toLowerCase();
	static final String BLTC_CLIENT = "BLCT4B0F12FA974043E3BB23D485237EAB64".toLowerCase();
	static final String RPLT_SERVICE = "/RPLT725B6C4B4302430CAC8C9181931C94B1".toLowerCase();
	static final String SESS_SERVICE = "/SESS5BE92DC6053640A08CCC123F68DD2F43".toLowerCase();
	static final String INST_SERVICE = "/INSTC9F9C186C0E34BB48E4E5864EF4F88E7".toLowerCase();
	static final String BLTC_SERVICE = "/BLSVA40D8A20683E4918A03DCF3461D04923".toLowerCase();
	
	static Logger log = Logger.getLogger(APIClient.class.getName());
	
	Client webClient;
	Client pollerClient;
	ObjectMapper mapper = new ObjectMapper();
	
	String host;
	String port;
	
	String blHost = "http://"+BLTC_CLIENT+".baltoro.io";
	//String host = "http://admin.baltoro.io";
	
	
	boolean online = false;
	
	APIClient()
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
	
		WebTarget target = webClient.target(blHost).path(BLTC_SERVICE+"/areyouthere");	
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		String str = response.readEntity(String.class);
		if(StringUtil.isNullOrEmpty(str))
		{
			System.out.println("can't reach server .. shutting down");
			System.exit(1);
		}
		log.info("response ==>"+str);
	}
	
	int getRemainingInsanceThreadsCount(String appUuid, String instanceUuid) throws Exception
	{
		log.info("... getInsanceThreadsCount ...");
	
		WebTarget target = webClient.target(blHost).path(INST_SERVICE+"/getRemainingInsanceThreadsCount");
		
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
	
		WebTarget target = webClient.target(blHost).path(INST_SERVICE+"/createinstance");
		
		Form form = new Form();
		form.param("app-uuid", appUuid);
		form.param("service-name", serviceName);
		if(StringUtil.isNullOrEmpty(instUuid) || !instUuid.startsWith("INST"))
		{
			instUuid = null;
		}
		form.param("inst-uuid", instUuid);
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		instUuid = response.readEntity(String.class);
		return instUuid;
	}
	

	
	String getAppUuidByName(String appName) throws Exception
	{
		log.info("... getting app uuid -> server ...");
	
		WebTarget target = webClient.target(blHost).path(BLTC_SERVICE+"/getAppUuidByName");
		
		Form form = new Form();
		form.param("app-name", appName);
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		String appUuid = response.readEntity(String.class);
		return appUuid;
	}
	
	String getAppData(String appUuid) throws Exception
	{
		log.info("... getting app data -> server ...");
	
		WebTarget target = webClient.target(blHost).path(BLTC_SERVICE+"/getAppData");
		
		Form form = new Form();
		form.param("app-uuid", appUuid);
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		String data = response.readEntity(String.class);
		return data;
	}
	
	String sendAppAPI() throws Exception
	{
		//log.info("... getting app data -> server ...");
		
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
		
	
		WebTarget target = webClient.target(blHost).path(BLTC_SERVICE+"/setappapi");
		
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
		//log.info("... getting app data -> server ...");
	
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
	
	
	
	ReplicationTO[] pullReplication(String lServerPushNano, String lServerPullNano) throws Exception
	{
		
		WebTarget target = webClient.target(blHost).path(RPLT_SERVICE+"/pull");
		String att = null;
		
		if(Baltoro.pullReplicationServiceNames == null)
		{
			att = Baltoro.serviceNames.toString();
		}
		else
		{
			att = Baltoro.serviceNames.toString()+" "+Baltoro.pullReplicationServiceNames;
		}
		
		log.info(" PULL ======= > "+att);
		
		Form form = new Form();
		form.param("appUuid", Baltoro.appUuid);
		form.param("instUuid", Baltoro.instanceUuid);
		form.param("att",att);
		form.param("lServerPullNano", lServerPullNano);
		form.param("lServerPushNano", lServerPushNano);
		form.param("initPull", LocalDB.initPull ? "YES" : "NO");
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		ReplicationTO[] tos = response.readEntity(ReplicationTO[].class);
		return tos;
	}
	
	int pullReplicationCount(Repl repl) throws Exception
	{
	 
		WebTarget target = webClient.target(blHost).path(RPLT_SERVICE+"/pullCount");
		String att = null;
		if(LocalDB.initPull)
		{
			att = Baltoro.serviceNames.toString()+" "+Baltoro.pullReplicationServiceNames;
		}
		else
		{
			att = Baltoro.pullReplicationServiceNames;
		}
		
		
		log.info(" PULL ======= > "+att);
		
		Form form = new Form();
		form.param("appUuid", Baltoro.appUuid);
		form.param("instUuid", Baltoro.instanceUuid);
		form.param("att", att);
		form.param("initPull", LocalDB.initPull ? "YES" : "NO");
		form.param("lServerPullNano", ""+repl.serverNano);
		
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		String _count = response.readEntity(String.class);
		
		int count = Integer.parseInt(_count);
		return count;
	}
	
	
	String pushReplication(String repObjJson) throws Exception
	{
		log.info("... push Replication ... ");
	
		WebTarget target = webClient.target(blHost).path(RPLT_SERVICE+"/push");
		
		Form form = new Form();
		form.param("app-uuid", Baltoro.appUuid);
		form.param("inst-uuid", Baltoro.instanceUuid);
		form.param("service-name", Baltoro.serviceNames.toString());
		form.param("json", repObjJson);
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		String res = response.readEntity(String.class);
		return res;
	}
	
	
	String validateSession(String sessionId, SessionUserTO to) throws Exception
	{
		log.info("... push create session ... ");
	
		WebTarget target = webClient.target(blHost).path(SESS_SERVICE+"/validate");
		
		Form form = new Form();
		form.param("app-uuid", Baltoro.appUuid);
		form.param("inst-uuid", Baltoro.instanceUuid);
		form.param("session-id", sessionId);
		String json = mapper.writeValueAsString(to);
		form.param("json", json);
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		String res = response.readEntity(String.class);
		return res;
	}
	
	String inValidateSession(String sessionId) throws Exception
	{
		log.info("... push remove session ... ");
	
		WebTarget target = webClient.target(blHost).path(SESS_SERVICE+"/invalidate");
		
		Form form = new Form();
		form.param("app-uuid", Baltoro.appUuid);
		form.param("inst-uuid", Baltoro.instanceUuid);
		form.param("session-id", sessionId);
		
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		String res = response.readEntity(String.class);
		return res;
	}
	
	SessionUserTO pullSession(String sessionId) throws Exception
	{
		log.info("... pull session ... ");
	
		WebTarget target = webClient.target(blHost).path(SESS_SERVICE+"/pull");
		
		Form form = new Form();
		form.param("app-uuid", Baltoro.appUuid);
		form.param("inst-uuid", Baltoro.instanceUuid);
		form.param("session-id", sessionId);
		
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		String res = response.readEntity(String.class);
		if(StringUtil.isNotNullAndNotEmpty(res))
		{
			return mapper.readValue(res, SessionUserTO.class);
		}
		
		return null;
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
