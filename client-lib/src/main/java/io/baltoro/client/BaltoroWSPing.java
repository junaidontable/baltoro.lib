package io.baltoro.client;

import java.util.concurrent.CountDownLatch;

import javax.websocket.Session;

public class BaltoroWSPing extends Thread
{

	Session session;
	long count=0;
	CountDownLatch latch;
	
	public BaltoroWSPing(CountDownLatch latch, Session session)
	{
		this.session = session;
		this.latch = latch;
	}
	
	public void run()
	{
		//for (int i = 0; i < 100; i++)
		while(true)
		{
			count++;
			try
			{
				session.getBasicRemote().sendText(""+count);
				Thread.sleep(5000);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
			finally
			{
				latch.countDown();
			}
		}
		
		
		latch.countDown();
	}
	
}
