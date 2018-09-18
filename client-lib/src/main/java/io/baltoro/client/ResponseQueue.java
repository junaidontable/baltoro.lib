package io.baltoro.client;



import java.util.concurrent.ConcurrentLinkedQueue;

import io.baltoro.to.WSTO;

public class ResponseQueue
{

	private static ResponseQueue responseQueue;
	private ConcurrentLinkedQueue q;
	
	private ResponseQueue()
	{
		q = new ConcurrentLinkedQueue<>();
	}
	
	
	static ResponseQueue instance()
	{
		if(responseQueue == null)
		{
			responseQueue = new ResponseQueue();
		}
		
		return responseQueue;
	}
	
	void addToResponseQueue(WSTO to)
	{
		
		q.add(to);
		
		String sync = "response-queue";
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
