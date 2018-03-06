package io.baltoro.client;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorkerPool
{

	private static ConcurrentLinkedQueue<RequestWorker> freeReq = new ConcurrentLinkedQueue<>();
	private static Set<RequestWorker> busyReq = new HashSet<>(50);
	
	private static ConcurrentLinkedQueue<ResponseWorker> freeRes = new ConcurrentLinkedQueue<>();
	private static Set<ResponseWorker> busyRes = new HashSet<>(50);
	
	static RequestWorker getRequestWorker()
	{
		if(freeReq.size() == 0)
		{
			return null;
		}
		
		
		RequestWorker worker = freeReq.poll();
		busyReq.add(worker);
		return worker;
	}
	
	static ResponseWorker getResponseWorker()
	{
		if(freeRes.size() == 0)
		{
			return null;
		}
		
		
		ResponseWorker worker = freeRes.poll();
		busyRes.add(worker);
		return worker;
	}
	
	static void done(RequestWorker worker)
	{
		worker.clear();
		busyReq.remove(worker);
		freeReq.add(worker);
	}
	
	static void done(ResponseWorker worker)
	{
		worker.clear();
		busyRes.remove(worker);
		freeRes.add(worker);
	}

	static String info()
	{
		return "freeReq("+freeReq.size()+"), busyReq("+busyReq.size()+"), freeRes("+freeRes.size()+"), busyRes("+busyRes.size()+")";
	}
	
}
