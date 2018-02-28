package io.baltoro.client;

import java.nio.ByteBuffer;

import io.baltoro.client.util.ObjectUtil;
import io.baltoro.to.WSTO;
import io.baltoro.to.WebSocketContext;

public class WSSession
{
	
	WSTO to = new WSTO();
	
	public WSSession(WSTO to)
	{
		this.to.appName = to.appName;
		this.to.appUuid = to.appUuid;
		this.to.instanceUuid = to.instanceUuid;
		
		
		WebSocketContext ctx = new WebSocketContext();
		ctx.setApiPath(to.webSocketContext.getApiPath());
		ctx.setInitRequestUuid(to.webSocketContext.getInitRequestUuid());
		ctx.setWsSessionUuid(to.webSocketContext.getWsSessionUuid());
		
		this.to.webSocketContext = ctx;
		
	}
	
	

	public void send(byte[] bytes)
	{
		
		this.to.webSocketContext.setData(bytes);
		try
		{
			
			bytes = ObjectUtil.toJason(this.to);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("CANNOT CONVERT TO JSON , !!!! CHECK !");
			return;
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		WSSessions.get().addToResponseQueue(buffer);
	}
}
