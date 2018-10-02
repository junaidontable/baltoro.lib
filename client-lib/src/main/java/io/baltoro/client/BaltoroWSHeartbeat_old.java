package io.baltoro.client;

public class BaltoroWSHeartbeat_old extends Thread
{

	/*
	int count=0;
	OperatingSystemMXBean os;
	MemoryMXBean mem;
	
	
	public BaltoroWSHeartbeat()
	{
		os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		mem = ManagementFactory.getPlatformMXBean(MemoryMXBean.class);
	}
	
	public void run()
	{
		
		while(true)
		{
			count++;
			try
			{
				WSTO to = new WSTO();
				to = new WSTO();
				to.instanceUuid = Baltoro.instanceUuid;
				to.appUuid = Baltoro.appUuid;
				to.appName = Baltoro.appName;
				
				
				MgntContext ctx = new MgntContext();
				if(count == 1)
				{
					Map<String, WebMethod> map = WebMethodMap.getInstance().getMap();
					List<PathTO> pathList = new ArrayList<>(200);
					
					for (String key : map.keySet())
					{
						WebMethod wm = map.get(key);
						
						PathTO pto = new PathTO();
						pto.appUuid = to.appUuid;
						pto.createdBy = to.instanceUuid;
						pto.path = key;
						pto.authRequired = wm.authRequired;
						pto.discoverable = wm.discoverable;
						pto.propsJson = wm.propJson;
						pto.timeoutSec = wm.timeoutSec;
						
						pathList.add(pto);
						System.out.println("PATH ADDING TO LIST -> "+key+" --> "+map.get(key));
					} 
					
					ctx.setPathTOs(pathList);
					
				
					
				}
				
				
				ctx.setThreadCount(Baltoro.instanceThreadCount);
				ctx.setServiceName(Baltoro.serviceNames.toString());
				ctx.setCpuPercent((int)os.getSystemLoadAverage());
				ctx.setServiceName(Baltoro.serviceNames.toString());
				
				long maxMem = (int) mem.getHeapMemoryUsage().getCommitted();
				long usedMem = (int) mem.getHeapMemoryUsage().getUsed();
				
				int freeMem = (int) (maxMem - usedMem)/1000000;
				
				ctx.setMemoryGB(freeMem);
				ctx.setLocalTimestamp(System.currentTimeMillis());
				ctx.setHeartBeatCount(count);
				//to.mgntContext = ctx;
				
				
				ResponseQueue.instance().addToResponseQueue(to);
				
			
				
				Thread.sleep(30000);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
		}
	
	}
	*/
	
}
