package io.baltoro.client;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestPoller extends Thread
{
	
	Baltoro baltoro;
	boolean run = true;
	
	public RequestPoller(Baltoro baltoro)
	{
		this.baltoro = baltoro;
	}

	@Override
	public void run()
	{
		while(run)
		{
			
			ConcurrentLinkedQueue<ByteBuffer> queue = WSSessions.get().getRequestQueue();
			if(queue == null || queue.size() == 0)
			{
				sleep("request queue is empty !");
				continue;
			}
			
			
			ByteBuffer byteBuffer = queue.peek();
			if(byteBuffer == null)
			{
				sleep(" No items in request queue !");
				continue;
			}
			
			
			
			System.out.println(" /////////////// ----- //////////// got buffer byte : ");
			byteBuffer = queue.poll();
			
			RequestWorker worker = new RequestWorker(byteBuffer);
			worker.start();
			
			
		}
	}
	
	private void sleep(String text)
	{
		try
		{
			long t0 = System.currentTimeMillis();
			String sync = "request-queue";
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
