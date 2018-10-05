package io.baltoro.client;

public class WSRequestPoller_old
{
	/*
	boolean run = true;
	static ObjectMapper mapper = new ObjectMapper();
	
	public WSRequestPoller()
	{
	}

	@Override
	public void run()
	{
		while(run)
		{
			
			ConcurrentLinkedQueue<ByteBuffer> queue = RequestQueue.instance().getQueue();
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
			WSTO to = getWSTO(byteBuffer);
			
			
			
			//System.out.println("Request >>  WorkerPool : "+WorkerPool.info());
			
			RequestWorker worker = WorkerPool.getRequestWorker();
			if(worker == null)
			{
				//System.out.println(" >>>>>>> worker is null creating new :::::::: ");
				worker = new RequestWorker();
				worker.start();
			}
			else
			{
				//System.out.println(" >>>>>>> exisitng worker :::::::: "+worker.count+" ,,,, "+worker);
			}
			
			//System.out.println(this+" >>>>>>> exisitng worker :::::::: "+worker.count+" ,,,, "+to.requestContext.getApiPath());
			worker.set(to);
			
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
	
	private WSTO getWSTO(ByteBuffer byteBuffer)
	{
		byte[] jsonBytes = byteBuffer.array();

		WSTO to = null;
		try
		{
			to = mapper.readValue(jsonBytes, WSTO.class);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		
		return to;
	}
	*/
	
}
