package io.baltoro.client;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ResponsePoller extends Thread
{
	
	private Baltoro baltoro;
	boolean run = true;
	
	public ResponsePoller(Baltoro baltoro)
	{
		this.baltoro = baltoro;
	}

	@Override
	public void run()
	{
		while(run)
		{
			
			ConcurrentLinkedQueue<ByteBuffer> queue = WSSessions.get().getResponseQueue();
			if(queue == null || queue.size() == 0)
			{
				sleep("response queue is empty !");
				continue;
			}
			
			
			ByteBuffer byteBuffer = queue.peek();
			if(byteBuffer == null)
			{
				sleep(" No items in response queue !");
				continue;
			}
				
			
			ClientWSSession session;
			try
			{
				session = WSSessions.get().getSession();
			} 
			catch (NoRunningSessionException e)
			{
				sleep("2 No running sessions for waiting the polling thread ");
				break;
			}
			
			if(session == null)
			{
				sleep(" no free session ! try again in 5 secs ");
				continue;
			}
			
			System.out.println(" /////////////////////////// got session  ----->   : "+session.getSession().getId());
			byteBuffer = queue.poll();
			
			ResponseWorker worker = new ResponseWorker(byteBuffer, session);
			worker.start();
			
			
		}
	}
	
	private void sleep(String text)
	{
		try
		{
			long t0 = System.currentTimeMillis();
			String sync = "response-queue";
			synchronized (sync.intern())
			{
				System.out.println(text);
				sync.intern().wait(50000);
				System.out.println("waited : "+(System.currentTimeMillis() - t0));
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
