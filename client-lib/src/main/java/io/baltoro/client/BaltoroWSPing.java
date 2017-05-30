package io.baltoro.client;

import javax.websocket.Session;

public class BaltoroWSPing extends Thread
{

	Session session;
	long count=0;
	
	public BaltoroWSPing(Session session)
	{
		this.session = session;
	}
	
	public void run()
	{
		//for (int i = 0; i < 100; i++)
		while(true)
		{
			count++;
			try
			{
				session.getAsyncRemote().sendText(""+count);
				System.out.println("sending ping "+count);
				Thread.sleep(5000);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
		}
	
	}
	
}
