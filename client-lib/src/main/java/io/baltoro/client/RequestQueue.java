package io.baltoro.client;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;




public class RequestQueue
{
	
	private static RequestQueue requestQueue;
	private ConcurrentLinkedQueue q;
	
	private RequestQueue()
	{
		q = new ConcurrentLinkedQueue<>();
	}
	
	
	static RequestQueue instance()
	{
		if(requestQueue == null)
		{
			requestQueue = new RequestQueue();
		}
		
		return requestQueue;
	}
	
	void addToRequestQueue(ByteBuffer byteBuffer)
	{
		
		q.add(byteBuffer);
		
		String sync = "request-queue";
		synchronized (sync.intern())
		{
			sync.intern().notify();
		}
		
	}
	
	ConcurrentLinkedQueue getQueue()
	{
		return q;
	}

}
