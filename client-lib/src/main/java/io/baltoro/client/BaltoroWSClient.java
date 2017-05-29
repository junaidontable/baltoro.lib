package io.baltoro.client;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

public class BaltoroWSClient extends Thread
{

	static CountDownLatch latch;
	private Logger log = Logger.getLogger(this.getClass().getName());
	private String appId;
	private String sessionId;
	
	BaltoroWSClient(String appId, String sessionId)
	{
		this.appId = appId;
		this.sessionId = sessionId;
	}
	
	
	public void run() 
	{
		latch = new CountDownLatch(1);

	    ClientManager client = ClientManager.createClient();
	    
	    BaltoroClientConfigurator clientConfigurator = new BaltoroClientConfigurator(sessionId);
	    
	    ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                .configurator(clientConfigurator)
                .build();
	   
	    try 
	    {
	    	
			BaltoroClientEndpoint instance = new BaltoroClientEndpoint(appId);
	    	Session session = client.connectToServer(instance, config, new URI("ws://"+this.appId+".baltoro.io/baltoro/ws"));
	    	log.info("got session "+session);
	        latch.await();
	    } 
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	    }
	}
}
