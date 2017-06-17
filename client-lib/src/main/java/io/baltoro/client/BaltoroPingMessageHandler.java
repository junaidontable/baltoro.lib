package io.baltoro.client;

import javax.websocket.MessageHandler;
import javax.websocket.PongMessage;
import javax.websocket.Session;

public class BaltoroPingMessageHandler implements MessageHandler.Whole<PongMessage>
{
	
	private Session session;
	private String appUuid;
	private String instanceUuid;
	
	public BaltoroPingMessageHandler(String appUuid, String instanceUuid, Session session)
	{
		this.session = session;
		this.appUuid = appUuid;
		this.instanceUuid = instanceUuid;
	}

	@Override
	public void onMessage(PongMessage msg)
	{
		System.out.println(" got ping back -> "+new String(msg.getApplicationData().array()));
		
	}
}
