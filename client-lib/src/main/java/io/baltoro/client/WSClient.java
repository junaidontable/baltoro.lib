package io.baltoro.client;

import java.net.URI;
import java.util.concurrent.Callable;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

class WSClient implements Callable<Session>
{
	
	private Baltoro baltoro;
	 
	WSClient(Baltoro baltoro)
	{
		this.baltoro = baltoro;
	}
	
	
	@Override
	public Session call() throws Exception
	{
		try 
	    {
			
			String token = System.currentTimeMillis()+"|"+baltoro.appUuid;
			String eToken = token;//CryptoUtil.encrypt(this.appPrivateKey, token.getBytes());
		
			
	    	ClientManager clientManager = ClientManager.createClient();
	 	    BaltoroClientConfigurator clientConfigurator = new BaltoroClientConfigurator(baltoro.agentCookieMap, baltoro.appUuid, baltoro.instanceUuid, eToken);
	 	    
	 	    ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
	                 .configurator(clientConfigurator)
	                 .build();
	 	    
	 	  
	 	  String url = null;
	 	  if(baltoro.debug)
	 	  {
	 		 url = "ws://"+baltoro.appUuid+".baltoro.io:8080/ws";
	 	  }
	 	  else
	 	  {
	 		 url = "ws://"+baltoro.appUuid+".baltoro.io/ws";
	 	  }
	 	  
	 	  BaltoroClientEndpoint instance = new BaltoroClientEndpoint(baltoro.appUuid, clientManager, config, url);
	 	 
	 	  Session session = clientManager.connectToServer(instance, config, new URI(url));
	 	  
	 	  return session ;
	 	 
	    }
	    catch (Exception e) 
	    {
	        throw new IllegalStateException("task interrupted", e);
	    }
	}
	
	
	

}
