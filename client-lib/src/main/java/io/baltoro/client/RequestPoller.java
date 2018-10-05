package io.baltoro.client;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.util.StringUtil;
import io.baltoro.to.WSTO;

public class RequestPoller// extends Thread
{
	
	private static RequestPoller poller;
	//boolean run = true;
	private ObjectMapper mapper = new ObjectMapper();
	private OperatingSystemMXBean os;
	private MemoryMXBean mem;
	private Timer pollTimer;
	private Timer workerTimer;
	
	private ConcurrentLinkedQueue<String> q;
	private String syncKey = "request-poll-worker";
	
	
	private RequestPoller()
	{
		os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		mem = ManagementFactory.getPlatformMXBean(MemoryMXBean.class);
		q = new ConcurrentLinkedQueue<>();
	}

	public static RequestPoller instance()
	{
		if(poller == null)
		{
			poller = new RequestPoller();
			poller.init();
		}
		
		return poller;
	}
	
	public void init()
	{
		
		
		pollTimer = new Timer();
		pollTimer.schedule(new TimerTask()
		{
			
			public void run()
			{
				System.out.println("############################### poll timer "+new Date());
				int cpu = (int) os.getSystemLoadAverage();
				
				long maxMem = (int) mem.getHeapMemoryUsage().getCommitted();
				long usedMem = (int) mem.getHeapMemoryUsage().getUsed();
				
				int freeMem = (int) (maxMem - usedMem)/1000000;
				
			
			
				String json = null;
				try
				{
					json = Baltoro.cs.poll(cpu, freeMem);
					if(StringUtil.isNotNullAndNotEmpty(json))
					{
						q.add(json);
						synchronized(syncKey.intern())
						{
							syncKey.notify();
						}
					}
				} 
				catch (Exception e)
				{
					//System.out.println(e.getCause());
					if(e.getCause() instanceof  SocketTimeoutException)
					{
						System.out.println("Read timeout, will try again ... "+e);
					}
					else
					{
						e.printStackTrace();
						System.exit(1);
					}
					
				}
					
			}
		}, 0,1);
		
		workerTimer = new Timer();
		workerTimer.schedule(new TimerTask()
		{
			
			public void run()
			{
				try
				{
					System.out.println("############################### worker timer "+new Date());
					String json = q.poll();
					if(json == null)
					{
						synchronized(syncKey.intern())
						{
							syncKey.wait(10000);
							return;
						}
					}
					WSTO[] tos = mapper.readValue(json, WSTO[].class);
					for (WSTO wsto : tos)
					{
						if(wsto.requestContext != null && wsto.requestContext.isInvalidateSession())
						{
							SessionManager.removeSession(wsto.requestContext.getSessionId());
						}
						else
						{
							RequestWorker worker = WorkerPool.getRequestWorker();
							worker.set(wsto);
						}
					}
					
				
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
					
			}
		}, 0,1);
		
	}
	
	
	
}
			
			/*
			if(StringUtil.isNullOrEmpty(json))
			{
				continue;
			}
			
			WSTO[] tos = readData(json);
		
			for (int i = 0; i < tos.length; i++)
			{
			
				WSTO to = tos[i];
				if(to.requestContext != null &&  to.requestContext.isInvalidateSession())
				{
					System.out.println(" @@@@@@@@@@@@@@@@@@ invalidating session : "+to.requestContext.getSessionId());
					SessionManager.removeSession(to.requestContext.getSessionId());
					continue;
				}
			
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
*/
