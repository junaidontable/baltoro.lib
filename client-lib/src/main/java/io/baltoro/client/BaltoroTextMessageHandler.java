package io.baltoro.client;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

public class BaltoroTextMessageHandler implements MessageHandler.Whole<String>
{
	
	private Session session;
	private String appId;
	
	public BaltoroTextMessageHandler(String appId, Session session)
	{
		this.session = session;
		this.appId = appId;
	}

	@Override
	public void onMessage(String message)
	{
		System.out.println(" got ping back -> "+message);
		
	}
}
