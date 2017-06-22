package io.baltoro.client;

import java.nio.ByteBuffer;

import javax.websocket.Session;

public class BaltoroWSPing extends Thread
{

	Session session;
	int count=0;
	Baltoro baltoro;
	
	public BaltoroWSPing(Baltoro baltoro, Session session)
	{
		this.baltoro = baltoro;
		this.session = session;
		}
	
	public void run()
	{
		
		while(true)
		{
			count++;
			try
			{
					
				ByteBuffer  msg = ByteBuffer.wrap((""+count).getBytes());
				session.getBasicRemote().sendPing(msg);
				
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
