package io.baltoro.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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


@ClientEndpoint
public class WSCountClient
{

	private static CountDownLatch latch;
	private Logger logger = Logger.getLogger(this.getClass().getName());


	@OnOpen
	public void onOpen(Session session)
	{
		logger.info("Connected ... " + session.getId());
      
		try
		{
			session.getBasicRemote().sendText("start");
			WSCountThread thread = new WSCountThread(session);
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
		logger.info(message);
		return null;
		
		/*
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        try {
            logger.info("Received ...." + message);
            String userInput = bufferRead.readLine();
            return userInput;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */
	}
	

	@OnClose
	public void onClose(javax.websocket.Session session, CloseReason closeReason)
	{
		logger.info(String.format("Session %s close because of %s", session, closeReason));
		latch.countDown();
	}

	public static void main(String[] args) 
	{
		latch = new CountDownLatch(1);

	    ClientManager client = ClientManager.createClient();
	    try 
	    {
	        client.connectToServer(WSCountClient.class, new URI("ws://localhost:8080/baltoro/ws?appid=xdcfd"));
	        latch.await();
	
	    } 
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	    }
	}
}
