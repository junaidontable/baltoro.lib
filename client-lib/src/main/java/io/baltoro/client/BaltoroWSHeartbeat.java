package io.baltoro.client;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Optional;

import javax.websocket.Session;

import io.baltoro.client.util.ObjectUtil;
import io.baltoro.to.MonitoringContext;
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
				to.instanceUuid = baltoro.instanceUuid;
				to.appUuid = baltoro.appUuid;
				to.appName = baltoro.appName;
				
				MonitoringContext ctx = new MonitoringContext();
				
				String localName =  Optional.ofNullable(InetAddress.getLocalHost().getHostName()).orElse(InetAddress.getLocalHost().getHostAddress());
				
				
				ctx.setHostName(localName);
				ctx.setCpuPercent((int)os.getSystemLoadAverage());
				
				long maxMem = (int) mem.getHeapMemoryUsage().getCommitted();
				long usedMem = (int) mem.getHeapMemoryUsage().getUsed();
				
				int freeMem = (int) (maxMem - usedMem)/1000000;
				
				ctx.setMemoryGB(freeMem);
				ctx.setLocalTimestamp(System.currentTimeMillis());
				ctx.setHeartBeatCount(count);
				to.monitoringContext = ctx;
				
				byte[]  bytes = ObjectUtil.toJason(to);
					
				
				ByteBuffer  msg = ByteBuffer.wrap(bytes);
				session.getBasicRemote().sendBinary(msg);
				
				System.out.println("sending monitoring "+count);
				Thread.sleep(5000);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				break;
			}
		}
	
	}
	
}
