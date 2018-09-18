package io.baltoro.client;

import java.util.concurrent.ConcurrentLinkedQueue;

import io.baltoro.to.WSTO;

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
			
			ConcurrentLinkedQueue<WSTO> queue = ResponseQueue.instance().getQueue();
			if(queue == null || queue.size() == 0)
			{
				wait("response queue is empty !");
				continue;
			}
			
			
			WSTO to = queue.poll();
			if(to == null)
			{
				wait(" No items in response queue !");
				continue;
			}
				
			
			ResponseWorker worker = WorkerPool.getResponseWorker();
			if(worker == null)
			{
				//System.out.println(" >>>>>>> worker is null creating new :::::::: ");
				worker = new ResponseWorker();
				worker.start();
			}
			else
			{
				//System.out.println(" >>>>>>> exisitng worker :::::::: "+worker.count+" ,,,, "+worker);
			}
			
			worker.set(to);
			
			//ResponseWorker worker = new ResponseWorker(byteBuffer, session);
			//worker.start();
			
			
		}
	}
	
	private void wait(String text)
	{
		try
		{
			//long t0 = System.currentTimeMillis();
			String sync = "response-queue";
			synchronized (sync.intern())
			{
				//System.out.println(text);
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
