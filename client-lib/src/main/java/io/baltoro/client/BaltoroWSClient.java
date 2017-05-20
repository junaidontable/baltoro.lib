package io.baltoro.client;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.to.WSTO;


@ClientEndpoint
public class BaltoroWSClient
{

	private static CountDownLatch latch;
	private Logger log = Logger.getLogger(this.getClass().getName());


	@OnOpen
	public void onOpen(Session session)
	{
		log.info("Connected ... " + session.getId());
      
		try
		{
			session.getBasicRemote().sendText("start");
			//BaltoroWSPing thread = new BaltoroWSPing(latch, session);
			//thread.start();
			//latch.countDown();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@OnMessage
	public String onMessage(String message, Session session)
	{
		log.info(" appid --- > text");
		
		
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			WSTO to = mapper.readValue(message.getBytes(),  WSTO.class);
			
			log.info("uuid-->"+to.uuid+", path --- >"+to.path);
			
			WebMethod wMethod = WebMethodMap.getInstance().getMethod(to.path);
			Class _class = wMethod.get_class();
			Method method = wMethod.getMethod();
			
			Object obj = _class.newInstance();
			Object returnObj = method.invoke(obj, null);
			
			log.info("execute method --- > "+returnObj);
			
			to.data = ((String)returnObj).getBytes();
			
			byte[] bytes = ObjectUtil.toJason(to);
			session.getAsyncRemote().sendText(new String(bytes, "utf-8"));
			
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;

	}
	
	/*
	@OnMessage
	//public void onBytes(byte[] b, boolean last, Session session) 
	public void onBytes(ByteBuffer bytes, Session session) 
	{
		
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			WSTO to = mapper.readValue(bytes.array(),  WSTO.class);
			
			log.info(" appid --- >"+to.appId);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		} 
		
	}
	*/

	@OnClose
	public void onClose(javax.websocket.Session session, CloseReason closeReason)
	{
		log.info(String.format("Session %s close because of %s", session, closeReason));
		latch.countDown();
	}

	public void start(String appId) 
	{
		latch = new CountDownLatch(1);

	    ClientManager client = ClientManager.createClient();
	    try 
	    {
	        //client.connectToServer(BaltoroWSClient.class, new URI("ws://localhost:8080/baltoro/ws?appid="+appId));
	    	client.connectToServer(BaltoroWSClient.class, new URI("ws://"+appId+".baltoro.io/baltoro/ws"));
	        latch.await();
	    } 
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	    }
	}
}
