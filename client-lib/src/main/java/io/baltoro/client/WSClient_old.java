package io.baltoro.client;

class WSClient_old
{
	
	 
	WSClient_old()
	{
		
	}
	
	/*
	@Override
	public Session call() throws Exception
	{
		try 
	    {
			
			String token = System.currentTimeMillis()+"|"+Baltoro.appUuid;
			String eToken = token;//CryptoUtil.encrypt(this.appPrivateKey, token.getBytes());
		
			
	    	ClientManager clientManager = ClientManager.createClient();
	 	    BaltoroClientConfig clientConfigurator = new BaltoroClientConfig(Baltoro.agentCookieMap, eToken);
	 	    
	 	    ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
	                 .configurator(clientConfigurator)
	                 .build();
	 	    
	 	  
	 	  String url = null;
	 	  if(Baltoro.debug)
	 	  {
	 		
	 		 //url = "ws://"+Baltoro.appUuid+".baltoro.io:8080/ws";
	 		  
	 		 url = "ws://"+Baltoro.appName+".baltoro.io:8080/ws";
	 		 
	 	  }
	 	  else
	 	  {
	 		 //url = "ws://"+Baltoro.appUuid+".baltoro.io/ws";
	 		 url = "ws://"+Baltoro.appName+".baltoro.io/ws";
	 	  }
	 	  
	 	 System.out.println("server url >>> "+url);
	 	 
	 	  BaltoroClientEndpoint instance = new BaltoroClientEndpoint(Baltoro.appUuid, clientManager, config, url);
	 	 
	 	  Session session = clientManager.connectToServer(instance, config, new URI(url));
	 	  
	 	  return session ;
	 	 
	    }
	    catch (Exception e) 
	    {
	        throw new IllegalStateException("task interrupted", e);
	    }
	}
	*/
	
	

}
