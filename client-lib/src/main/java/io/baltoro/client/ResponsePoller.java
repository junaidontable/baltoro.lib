package io.baltoro.client;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.websocket.Session;

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
			
			ConcurrentLinkedQueue<WSTO> queue = WSSessions.get().getResponseQueue();
			if(queue == null || queue.size() == 0)
			{
				wait("response queue is empty !");
				continue;
			}
			
			
			WSTO to = queue.peek();
			if(to == null)
			{
				wait(" No items in response queue !");
				continue;
			}
				
			
			Session session = WSSessions.get().getSessionForWork();
				
			if(session == null)
			{
				wait(" no free session ! try again in 50 secs ");
				continue;
			}
			
			to = queue.poll();
			
			//System.out.println("Response >>  WorkerPool : "+WorkerPool.info());
			
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
			
			worker.set(to, session );
			
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
