package io.baltoro.client;

import javax.websocket.Session;

public class WSCountThread extends Thread
{

	Session session;
	
	public WSCountThread(Session session)
	{
		this.session = session;
	}
	
	public void run()
	{
		for (int i = 0; i < 100; i++)
		{
			try
			{
				session.getBasicRemote().sendText(""+i);
				Thread.sleep(1000);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
		}
		
	}
	
}
