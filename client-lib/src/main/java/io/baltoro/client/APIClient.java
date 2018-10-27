package io.baltoro.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.util.StreamUtil;
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
	static final String UPFL_SERVICE = "/UPFL4CC05B478C4B4975917BA14A92DFCBE3".toLowerCase();
	
	static Logger log = Logger.getLogger(APIClient.class.getName());
	
	Client webClient;
	Client pollerClient;
	Client rawClient;
	
	ObjectMapper mapper = new ObjectMapper();
	
	
	String blHost;
	
	
	boolean online = false;
	
	APIClient()
	{
		
		blHost = Baltoro.serverURL;
		
		RequestFilter reqFilter = new RequestFilter();
		
		CheckResponseFilter responseFilter = new CheckResponseFilter();
		
		
		webClient = ClientBuilder.newBuilder()
				.register(JacksonFeature.class)
				.register(CheckRequestFilter.class)
				.register(reqFilter)
				.register(responseFilter)
				.register(MultiPartWriter.class)
				.build();
		webClient.property(ClientProperties.CONNECT_TIMEOUT, 5000);
		webClient.property(ClientProperties.READ_TIMEOUT, 60000);
		
		
		
		pollerClient = ClientBuilder.newBuilder()
				.register(JacksonFeature.class)
				.register(reqFilter)
				.build();
		
		pollerClient.property(ClientProperties.CONNECT_TIMEOUT, 5000);
		pollerClient.property(ClientProperties.READ_TIMEOUT, 60000);
		
		rawClient = ClientBuilder.newBuilder()
					.register(reqFilter)
					.build();
		
		rawClient.property(ClientProperties.CONNECT_TIMEOUT, 5000);
		rawClient.property(ClientProperties.READ_TIMEOUT, 60000);
		
	
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
	
	
	String areYouThere() throws Exception
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
		return str;
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
		form.param("service-name", Baltoro.serviceNames.toString());
		form.param("json", json);
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		String data = response.readEntity(String.class);
		return data;
	}
	
	String sendAPIResponse(String toUuid, String json, byte[] bytes) throws Exception
	{
		//log.info("... getting app data -> server ...");
	
		WebTarget target = webClient.target(blHost).path(RESP_SERVICE);
		
		if(bytes != null)
		{
		
		
			log.info(".>>>>>>>>>>>>>>>>>>>>>>>>>>> .. sending binary data -> server ...");
			StreamDataBodyPart dataPart = new StreamDataBodyPart("content", new ByteArrayInputStream(bytes));
			
				
			FormDataMultiPart form = new FormDataMultiPart();
			  form.field("app-uuid", Baltoro.appUuid);
			  form.field("inst-uuid", Baltoro.instanceUuid);
			  form.field("to-uuid", toUuid);
			  form.field("json", json);
			  form.bodyPart(dataPart);
			

			  
			Invocation.Builder ib =	getIB(target);
			
			
			
			Response response = ib.post( Entity.entity(form, MediaType.MULTIPART_FORM_DATA_TYPE));
			   
			String data = response.readEntity(String.class);
			form.close();
			response.close();
			return data;
			
		}
		
		Form form = new Form();
		form.param("app-uuid", Baltoro.appUuid);
		form.param("inst-uuid", Baltoro.instanceUuid);
		form.param("to-uuid", toUuid);
		form.param("json", json);
		
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		String data = response.readEntity(String.class);
		response.close();
		return data;
	}
	
	
	Builder getIB(WebTarget target)
	{
		Invocation.Builder ib =	target.request(MediaType.APPLICATION_JSON_TYPE);
		Set<String> cookieNames = Baltoro.cookieMap.keySet();
		
		StringBuffer buffer = new StringBuffer();
		for (String cookieName : cookieNames)
		{
			NewCookie cookie = Baltoro.cookieMap.get(cookieName);
			//log.info("sending ============= >>>>>> ["+Baltoro.agentCookieMap.hashCode()+"]>>>>> "+cookieName+" : "+cookie);
			String _cookie = cookie.getName()+"="+cookie.getValue()+";";
			buffer.append(_cookie);
		}
		
		ib.header("Cookie", buffer.toString());
	
		return ib;
	}
	
	
	private static String serviceAtt = null;
	
	ReplicationTO[] pullReplication(String lServerPushNano, String lServerPullNano) throws Exception
	{
		
		WebTarget target = webClient.target(blHost).path(RPLT_SERVICE+"/pull");
		if(serviceAtt == null)
		{
		
			StringBuffer att = new StringBuffer();
			
			if(Baltoro.pullReplicationServiceNames != null)
			{
				att.append(Baltoro.pullReplicationServiceNames.toString());
			}
			
			att.append(" ");
			
			String[] sNames = Baltoro.serviceNames.toString().split(",");
			for (int i = 0; i < sNames.length; i++)
			{
				String sName = sNames[i].toUpperCase();
				att.append(" service:"+sName+" ");
			}
			
			serviceAtt = att.toString();
		}
		
		//log.info(" PULL ======= > "+att);
		
		Form form = new Form();
		form.param("appUuid", Baltoro.appUuid);
		form.param("instUuid", Baltoro.instanceUuid);
		form.param("att",serviceAtt);
		form.param("lServerPullNano", lServerPullNano);
		form.param("lServerPushNano", lServerPushNano);
		//form.param("initPull", LocalDB.initPull ? "YES" : "NO");
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		ReplicationTO[] tos = response.readEntity(ReplicationTO[].class);
		return tos;
	}
	
	int pullReplicationCount(String lServerPushNano, String lServerPullNano) throws Exception
	{
	 
		WebTarget target = webClient.target(blHost).path(RPLT_SERVICE+"/pullCount");
		String att = Baltoro.serviceNames.toString();
		/*
		if(LocalDB.initPull)
		{
			att = Baltoro.serviceNames.toString()+" "+Baltoro.pullReplicationServiceNames;
		}
		else
		{
			att = Baltoro.pullReplicationServiceNames;
		}
		*/
		
		log.info(" PULL ======= > "+att);
		
		Form form = new Form();
		form.param("appUuid", Baltoro.appUuid);
		form.param("instUuid", Baltoro.instanceUuid);
		form.param("att", att);
		//form.param("initPull", LocalDB.initPull ? "YES" : "NO");
		form.param("lServerPullNano", lServerPullNano);
		form.param("lServerPushNano", lServerPushNano);
		
		
		
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
		//log.info("1... polling data  -> server ... "+Baltoro.appName+" ,,,, "+Baltoro.serviceNames.toString());
	
	
		WebTarget target = pollerClient.target(blHost)
				.path(POLL_SERVICE)
				.queryParam("BLT_CPU", cpu)
				.queryParam("BLT_MEMORY_GB", memoryGB);
				
		
		
		
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		String json = response.readEntity(String.class);
		
		
		return json;
	}
	
	
	byte[] pullUploadedFileData(String uuid) throws Exception
	{
		log.info("... pull uploaded file ... ");
	
		WebTarget target = rawClient.target(blHost).path(UPFL_SERVICE+"/pull").queryParam("uuid", uuid);
		
		byte[] bytes = null;
		try (InputStream in = target.request(MediaType.APPLICATION_OCTET_STREAM).get(InputStream.class);) 
		{
			bytes = StreamUtil.toBytes(in);//ByteStreams.toByteArray(in);// StreamUtil.toBytes(in);
        }
		
		return bytes;
	}
	

}
