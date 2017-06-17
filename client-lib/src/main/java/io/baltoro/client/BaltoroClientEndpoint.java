package io.baltoro.client;

import java.util.logging.Logger;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;


public class BaltoroClientEndpoint extends Endpoint
{

	private Logger log = Logger.getLogger(this.getClass().getName());
	private String appUuid;
	private String instanceUuid;
	private ClientManager clientManager;
	private ClientEndpointConfig config;
	private String url;
	
	
	public BaltoroClientEndpoint(String appUuid, ClientManager clientManager, ClientEndpointConfig config, String url)
	{
		this.appUuid = appUuid;
		this.clientManager = clientManager;
		this.config = config;
		this.url = url;
	}
	
	
	
	public void onOpen(Session session, EndpointConfig config)
	{
		log.info(" ******** Connected ... " + session.getId());
		
		session.addMessageHandler(new BaltoroTextMessageHandler(this.appUuid, this.instanceUuid,session));
		session.addMessageHandler(new BaltoroByteBufferMessageHandler(this.appUuid, this.instanceUuid, session));
		session.addMessageHandler(new BaltoroPingMessageHandler(this.appUuid, this.instanceUuid, session));
	}
	
	



	@OnClose
	public void onClose(javax.websocket.Session session, CloseReason closeReason)
	{
		log.info(String.format("Session close because of %s", closeReason));
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
