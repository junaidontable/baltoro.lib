package io.baltoro.client;

public class BaltoroWSClient
{

	/*
	CountDownLatch wsLatch;
	private Logger log = Logger.getLogger(this.getClass().getName());
	private String appId;
	private String sessionId;
	private Session session;
	
	public Session getSession()
	{
		return session;
	}


	BaltoroWSClient(String appId, String sessionId)
	{
		this.appId = appId;
		this.sessionId = sessionId;
	}
	
	
	public void run() 
	{
		wsLatch = new CountDownLatch(1);

	    ClientManager client = ClientManager.createClient();
	    
	    BaltoroClientConfigurator clientConfigurator = new BaltoroClientConfigurator(sessionId);
	    
	    ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                .configurator(clientConfigurator)
                .build();
	   
	    try 
	    {
	    	
			BaltoroClientEndpoint instance = new BaltoroClientEndpoint(appId);
	    	this.session = client.connectToServer(instance, config, new URI("ws://"+this.appId+".baltoro.io/baltoro/ws"));
	    	log.info("got session "+session);
	    	wsLatch.await();
	    } 
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	    }
	}
	*/
}
