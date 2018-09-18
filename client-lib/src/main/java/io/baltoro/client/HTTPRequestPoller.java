package io.baltoro.client;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.util.StringUtil;

public class HTTPRequestPoller extends Thread
{
	
	boolean run = true;
	static ObjectMapper mapper = new ObjectMapper();
	OperatingSystemMXBean os;
	MemoryMXBean mem;
	
	
	public HTTPRequestPoller()
	{
		os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		mem = ManagementFactory.getPlatformMXBean(MemoryMXBean.class);
	}

	@Override
	public void run()
	{
		while(run)
		{
			
			int cpu = (int) os.getSystemLoadAverage();
			
			long maxMem = (int) mem.getHeapMemoryUsage().getCommitted();
			long usedMem = (int) mem.getHeapMemoryUsage().getUsed();
			
			int freeMem = (int) (maxMem - usedMem)/1000000;
			
		
		
			String json = Baltoro.cs.poll(cpu, freeMem);
			if(StringUtil.isNullOrEmpty(json))
			{
				continue;
			}
			
			List list = readData(json);
		
			for (int i = 0; i < list.size(); i++)
			{
				String str = (String) list.get(i);
				System.out.println(" ===========> str "+str);
			}
			
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
			worker.set(null);
			
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
	
	private List<byte[]> readData(String json)
	{
		byte[] jsonBytes = json.getBytes();
		

		List<byte[]> list = null;
		try
		{
			list = mapper.readValue(jsonBytes, List.class);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		
		return list;
	}
	
}
