package io.baltoro.client;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;


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
			BaltoroWSPing thread = new BaltoroWSPing(latch, session);
			thread.start();
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
		log.info(message);
		return null;

	}
	
	@OnMessage
	//public void onBytes(byte[] b, boolean last, Session session) 
	public void onBytes(ByteBuffer bytes, Session session) 
	{
	    log.info(bytes.toString());
	}
	

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
	        client.connectToServer(BaltoroWSClient.class, new URI("ws://localhost:8080/baltoro/ws?appid="+appId));
	        latch.await();
	    } 
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	    }
	}
}
