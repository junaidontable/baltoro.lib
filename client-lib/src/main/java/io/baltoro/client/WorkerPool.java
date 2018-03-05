package io.baltoro.client;

import java.util.concurrent.ConcurrentLinkedQueue;

public class WorkerPool
{

	//private static Set<RequestWorker> freeRW = new HashSet<>(50);
	//private static Set<RequestWorker> workingRW = new HashSet<>(50);
	
	
	//private static List<RequestWorker> freeRW = new ArrayList<>(50);
	//private static List<RequestWorker> workingRW = new ArrayList<>(50);
	
	private static ConcurrentLinkedQueue<RequestWorker> free = new ConcurrentLinkedQueue<>();
	private static ConcurrentLinkedQueue<RequestWorker> busy = new ConcurrentLinkedQueue<>();
	
	static RequestWorker get()
	{
		if(free.size() == 0)
		{
			return null;
		}
		
		
		RequestWorker worker = free.poll();
		busy.add(worker);
		return worker;
	}
	
	static void done(RequestWorker worker)
	{
		worker.clear();
		busy.remove(worker);
		free.add(worker);
	}
	
	
	static void add(RequestWorker worker)
	{
		busy.add(worker);

	}
	

	static String info()
	{
		return "free : "+free.size()+" busy : "+busy.size();
	}
	
}
