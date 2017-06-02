package io.baltoro.client;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

public class BaltoroTextMessageHandler implements MessageHandler.Whole<String>
{
	
	private Session session;
	private String appUuid;
	private String instanceUuid;
	
	public BaltoroTextMessageHandler(String appUuid, String instanceUuid, Session session)
	{
		this.session = session;
		this.appUuid = appUuid;
		this.instanceUuid = instanceUuid;
	}

	@Override
	public void onMessage(String message)
	{
		System.out.println(" got ping back -> "+message);
		
	}
}
