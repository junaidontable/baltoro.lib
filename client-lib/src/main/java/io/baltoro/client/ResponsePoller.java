package io.baltoro.client;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ResponsePoller extends Thread
{
	
	
	boolean run = true;
	
	public ResponsePoller()
	{
	}

	@Override
	public void run()
	{
		while(run)
		{
			
			int count = WSSessions.get().checkSessions();
			{
				if(count == 0)
				{
					System.out.println("No running sessions plz restart the instance ");
					System.exit(1);
				}
				else
				{
					//System.out.println("total valid connections ["+count+"] ");
				}
			}
			
			ConcurrentLinkedQueue<ByteBuffer> queue = WSSessions.get().getResponseQueue();
			if(queue == null || queue.size() == 0)
			{
				//sleep("response queue is empty !");
				continue;
			}
			
			
			ByteBuffer byteBuffer = queue.peek();
			if(byteBuffer == null)
			{
				sleep(" No items in response queue !");
				continue;
			}
				
			
			ClientWSSession session = WSSessions.get().getSession();
				
			if(session == null)
			{
				sleep(" no free session ! try again in 5 secs ");
				continue;
			}
			
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
				//System.out.println("client lib response waited : "+(System.currentTimeMillis() - t0));
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
