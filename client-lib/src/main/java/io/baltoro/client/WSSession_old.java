package io.baltoro.client;

public class WSSession_old
{
	
	/*
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
		
		WSTO _to = new WSTO();
		_to.appName = to.appName;
		_to.appUuid = to.appUuid;
		_to.instanceUuid = to.instanceUuid;
		
		WebSocketContext ctx = new WebSocketContext();
		ctx.setApiPath(to.webSocketContext.getApiPath().replace("onopen", "onmessage"));
		ctx.setData(bytes);
		ctx.setInitRequestUuid(to.uuid);
		ctx.setWsSessionUuid(to.webSocketContext.getWsSessionUuid());
		_to.webSocketContext = ctx;
		
	
		
		//ByteBuffer buffer = ByteBuffer.wrap(bytes);
		ResponseQueue.instance().addToResponseQueue(_to);
	}
	
	public void send(String msg)
	{
		
		WSTO _to = new WSTO();
		_to.appName = to.appName;
		_to.appUuid = to.appUuid;
		_to.instanceUuid = to.instanceUuid;
		
		WebSocketContext ctx = new WebSocketContext();
		ctx.setApiPath(to.webSocketContext.getApiPath().replace("onopen", "onmessage"));
		ctx.setMessage(msg);
		ctx.setInitRequestUuid(to.uuid);
		ctx.setWsSessionUuid(to.webSocketContext.getWsSessionUuid());
		_to.webSocketContext = ctx;
		
	
		
		ResponseQueue.instance().addToResponseQueue(_to);
	}
	*/
}
