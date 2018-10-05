package io.baltoro.client;

public class RequestQueue_old
{
	
	/*
	private static RequestQueue requestQueue;
	private ConcurrentLinkedQueue<String> q;
	
	private RequestQueue_old()
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
	
	void addToRequestQueue(String json)
	{
		
		q.add(json);
		
		String sync = "request-queue";
		synchronized (sync.intern())
		{
			sync.intern().notify();
		}
		
	}
	
	ConcurrentLinkedQueue<String> getQueue()
	{
		return q;
	}
	*/
}
