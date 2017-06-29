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

import javax.websocket.Session;

import io.baltoro.client.util.ObjectUtil;
import io.baltoro.to.MgntContext;
import io.baltoro.to.PathTO;
import io.baltoro.to.WSTO;

public class BaltoroWSHeartbeat extends Thread
{

	Session session;
	int count=0;
	OperatingSystemMXBean os;
	MemoryMXBean mem;
	Baltoro baltoro;
	
	public BaltoroWSHeartbeat(Baltoro baltoro, Session session)
	{
		this.baltoro = baltoro;
		this.session = session;
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
				to.instanceUuid = baltoro.instanceUuid;
				to.appUuid = baltoro.appUuid;
				to.appName = baltoro.appName;
				
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
				}
				
				String localName =  Optional.ofNullable(InetAddress.getLocalHost().getHostName()).orElse(InetAddress.getLocalHost().getHostAddress());
				
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
				session.getBasicRemote().sendBinary(msg);
				
				System.out.println("sending monitoring "+count);
				
				
				Thread.sleep(30000);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
		}
	
	}
	
	public Session getSession()
	{
		return session;
	}
	
}
