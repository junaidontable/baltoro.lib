package io.baltoro.client;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.baltoro.client.util.ObjectUtil;
import io.baltoro.to.MgntContext;
import io.baltoro.to.PathTO;
import io.baltoro.to.WSTO;

public class BaltoroWSHeartbeat extends Thread
{

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
				
				String localName = null;
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
						
						pathList.add(pto);
						System.out.println("PATH ADDING TO LIST -> "+key+" --> "+map.get(key));
					} 
					
					ctx.setPathTOs(pathList);
					
				
					localName =  new String(InetAddress.getLocalHost().getAddress());
				}
				
				
				ctx.setClusterPath(Baltoro.clusterPath);
				ctx.setHostName(localName);
				ctx.setCpuPercent((int)os.getSystemLoadAverage());
				
				long maxMem = (int) mem.getHeapMemoryUsage().getCommitted();
				long usedMem = (int) mem.getHeapMemoryUsage().getUsed();
				
				int freeMem = (int) (maxMem - usedMem)/1000000;
				
				ctx.setMemoryGB(freeMem);
				ctx.setLocalTimestamp(System.currentTimeMillis());
				ctx.setHeartBeatCount(count);
				to.mgntContext = ctx;
				
				byte[]  bytes = ObjectUtil.toJason(to);
					
				
				ByteBuffer  msg = ByteBuffer.wrap(bytes);
				
				WSSessions.get().addToResponseQueue(msg);
				
				/*
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				System.out.println("sending monitoring "+count);
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				*/
				
				
				Thread.sleep(30000);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
		}
	
	}
	
	
}
