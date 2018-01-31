package io.baltoro.client;

import java.net.URI;
import java.util.concurrent.Callable;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

class WSClient implements Callable<Session>
{
	
	 
	WSClient()
	{
		
	}
	
	
	@Override
	public Session call() throws Exception
	{
		try 
	    {
			
			String token = System.currentTimeMillis()+"|"+Baltoro.appUuid;
			String eToken = token;//CryptoUtil.encrypt(this.appPrivateKey, token.getBytes());
		
			
	    	ClientManager clientManager = ClientManager.createClient();
	 	    BaltoroClientConfig clientConfigurator = new BaltoroClientConfig(Baltoro.agentCookieMap, Baltoro.appUuid, Baltoro.instanceUuid, eToken);
	 	    
	 	    ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
	                 .configurator(clientConfigurator)
	                 .build();
	 	    
	 	  
	 	  String url = null;
	 	  if(Baltoro.debug)
	 	  {
	 		 url = "ws://"+Baltoro.appUuid+".baltoro.io:8080/ws";
	 	  }
	 	  else
	 	  {
	 		 url = "ws://"+Baltoro.appUuid+".baltoro.io/ws";
	 	  }
	 	  
	 	  BaltoroClientEndpoint instance = new BaltoroClientEndpoint(Baltoro.appUuid, clientManager, config, url);
	 	 
	 	  Session session = clientManager.connectToServer(instance, config, new URI(url));
	 	  
	 	  return session ;
	 	 
	    }
	    catch (Exception e) 
	    {
	        throw new IllegalStateException("task interrupted", e);
	    }
	}
	
	
	

}
