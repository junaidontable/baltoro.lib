package io.baltoro.client;

import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.Session;


public class BaltoroClientEndpoint extends Endpoint
{

	private Logger log = Logger.getLogger(this.getClass().getName());
	private String appId;
	
	
	
	public BaltoroClientEndpoint(String appId)
	{
		this.appId = appId;
	}
	
	
	
	public void onOpen(Session session, EndpointConfig config)
	{
		log.info(" ******** Connected ... " + session.getId());
		
		session.addMessageHandler(new BaltoroTextMessageHandler(this.appId, session));
		session.addMessageHandler(new BaltoroByteBufferMessageHandler(this.appId, session));
	}
	
	



	@OnClose
	public void onClose(javax.websocket.Session session, CloseReason closeReason)
	{
		log.info(String.format("Session %s close because of %s", session, closeReason));
		//latch.countDown();
	}
	
	

}
