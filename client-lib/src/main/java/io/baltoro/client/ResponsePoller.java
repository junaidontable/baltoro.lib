package io.baltoro.client;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.websocket.Session;

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
				wait("response queue is empty !");
				continue;
			}
			
			
			ByteBuffer byteBuffer = queue.peek();
			if(byteBuffer == null)
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
			
			byteBuffer = queue.poll();
			
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
			
			worker.set(byteBuffer, session );
			
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
