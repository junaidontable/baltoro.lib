package io.baltoro.client;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestPoller extends Thread
{
	
	boolean run = true;
	
	public RequestPoller()
	{
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
			
			
			
			byteBuffer = queue.poll();
			
			
			System.out.println(" WorkerPool : "+WorkerPool.info());
			
			RequestWorker worker = WorkerPool.get();
			if(worker == null)
			{
				System.out.println(" >>>>>>> worker is null creating new :::::::: ");
				worker = new RequestWorker();
				worker.start();
				WorkerPool.add(worker);
			}
			else
			{
				System.out.println(" >>>>>>> exisitng worker :::::::: "+worker.count+" ,,,, "+worker);
			}
			
			worker.set(byteBuffer);
			
			//RequestWorker worker = new RequestWorker(byteBuffer);
			//worker.start();
			
			
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
				//System.out.println(text);
				sync.intern().wait(50000);
				//System.out.println("client lib server waited : "+(System.currentTimeMillis() - t0));
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
