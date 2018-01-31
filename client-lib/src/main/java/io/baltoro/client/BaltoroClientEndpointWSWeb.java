package io.baltoro.client;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.Session;


public class BaltoroClientEndpointWSWeb extends Endpoint
{

	private static Logger log = Logger.getLogger(BaltoroClientEndpointWSWeb.class.getName());
	private String appName;
	private String path;
	MessageHandler.Whole<ByteBuffer> handlerClass;
	
	
	public BaltoroClientEndpointWSWeb(String appName, String path, MessageHandler.Whole<ByteBuffer> handlerClass)
	{
		this.appName = appName;
		this.path = path;
		this.handlerClass = handlerClass;
	}
	
	
	
	public void onOpen(Session session, EndpointConfig config)
	{
		log.info(" ******** Connected ... " + session.getId());
		session.addMessageHandler(handlerClass);
		//session.addMessageHandler(new BaltoroTextMessageHandler(this.appUuid, this.instanceUuid,session));
		//session.addMessageHandler(new BaltoroByteBufferMessageHandler(this.appUuid, this.instanceUuid, session));
		//session.addMessageHandler(new BaltoroPingMessageHandler(this.appUuid, this.instanceUuid, session));
	}
	
	



	@OnClose
	public void onClose(javax.websocket.Session session, CloseReason closeReason)
	{
		
		WSSessions.get().removeSession(session);
		
		
		//log.info(String.format("Session close because of %s", closeReason));
		try
		{
			//Session _session = clientManager.connectToServer(this, config, new URI(url));
			log.info(String.format("Session %s close because of %s", session, closeReason));
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	

}
