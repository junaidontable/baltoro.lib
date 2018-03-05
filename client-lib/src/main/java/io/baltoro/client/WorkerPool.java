package io.baltoro.client;

import java.util.ArrayList;
import java.util.List;

public class WorkerPool
{

	//private static Set<RequestWorker> freeRW = new HashSet<>(50);
	//private static Set<RequestWorker> workingRW = new HashSet<>(50);
	
	
	private static List<RequestWorker> freeRW = new ArrayList<>(50);
	private static List<RequestWorker> workingRW = new ArrayList<>(50);
	
	static RequestWorker get()
	{
		if(freeRW.size() == 0)
		{
			return null;
		}
		
		
		RequestWorker worker = freeRW.get(0);
		
		
		freeRW.remove(worker);
		workingRW.add(worker);
		
		return worker;
	}
	
	static void done(RequestWorker worker)
	{
		worker.clear();
		workingRW.remove(worker);
		freeRW.add(worker);
	}
	
	
	static void add(RequestWorker worker)
	{
		workingRW.add(worker);

	}
	

	static String info()
	{
		return "free : "+freeRW.size()+" busy : "+workingRW.size();
	}
	
}
