package io.baltoro.client;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.ConnectException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.to.WSTO;
import io.baltoro.util.StringUtil;

public class RequestPoller extends Thread
{
	
	boolean run = true;
	static ObjectMapper mapper = new ObjectMapper();
	OperatingSystemMXBean os;
	MemoryMXBean mem;
	
	
	public RequestPoller()
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
			
		
		
			String json = null;
			try
			{
				json = Baltoro.cs.poll(cpu, freeMem);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				System.exit(1);
			}
			
			
			if(StringUtil.isNullOrEmpty(json))
			{
				continue;
			}
			
			WSTO[] tos = readData(json);
		
			for (int i = 0; i < tos.length; i++)
			{
			
				WSTO to = tos[i];
				//System.out.println(" ===========> to "+to);
			
			
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
			
			}
			
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
	
	private WSTO[] readData(String json)
	{
		byte[] jsonBytes = json.getBytes();
		

		WSTO[] tos = null;
		try
		{
			tos = mapper.readValue(jsonBytes, WSTO[].class);
			
			//System.out.println(" to ===== > "+tos.length);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		
		return tos;
	}
	
}
