package io.baltoro.client;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.ByteBuffer;

import javax.websocket.Session;

import io.baltoro.client.util.ObjectUtil;
import io.baltoro.to.WSTO;

public class BaltoroWSPing extends Thread
{

	Session session;
	long count=0;
	OperatingSystemMXBean os;
	
	public BaltoroWSPing(Session session)
	{
		this.session = session;
		os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
	}
	
	public void run()
	{
		//for (int i = 0; i < 100; i++)
		while(true)
		{
			count++;
			try
			{
				
				WSTO to = new WSTO();
				//to.cpuPercent = Integer.to os.getSystemLoadAverage();
				//to.memoryGB = os.
				
				//byte[] bytes = ObjectUtil.toJason(to);
				//ByteBuffer buffer = ByteBuffer.wrap(bytes);
				//session.getAsyncRemote().sendBinary(buffer);
				
				
				String _count = ""+count;
				ByteBuffer  msg = ByteBuffer.wrap(_count.getBytes());
				session.getBasicRemote().sendPing(msg);
				System.out.println("sending ping "+count);
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
