package io.baltoro.client;

import java.nio.ByteBuffer;

import javax.websocket.Session;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.to.WSTO;
import io.baltoro.util.ObjectUtil;

public class ResponseWorker extends  Thread
{
	private WSTO to;
	private Session session;

	static ObjectMapper objectMapper = new ObjectMapper();
	
	boolean run = true;
	static int _count;
	int count;
	
	public ResponseWorker()
	{
		synchronized (ResponseWorker.class.getName().intern())
		{
			this.count = _count++;
		}
		
	}
	
	void set(WSTO to, Session session)
	{
		this.to = to;
		this.session = session;
		
		synchronized (this)
		{
			this.notify();
		}
	}
	
	void clear()
	{
		this.to = null;
		this.session = null;
	}
	
	@Override
	public void run()
	{
		while (run)
		{
			if(to == null || session == null)
			{
				synchronized (this)
				{
					try
					{
						//System.out.println("worker before waiting ..... "+this+",  --- "+count);
						
						this.wait(10000);
						
						//System.out.println("worker after waiting ..... "+this+",  --- "+count);
						
						if(to == null || session == null)
						{
							//System.out.println("RESPONSE thread no work to do  "+this+",  --- "+count+",,,"+WorkerPool.info());
							continue;
						}
					} 
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			
			try
			{
				/*
				String url = to.requestContext != null ? to.requestContext.getApiPath() : "null";
				System.out.println("^^^^^^^^^ response triger :: url"+url+", uuid:"+to.uuid);
				
				int len = to.responseContext != null ? to.responseContext.getData().length : -1;
				System.out.println("^^^^^^^^^ response bytes :: "+len);
				*/
				
				to.requestContext = null;
				
				byte[] json = ObjectUtil.toJason(to);
				ByteBuffer byteBuffer = ByteBuffer.wrap(json);
				session.getBasicRemote().sendBinary(byteBuffer);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally 
			{
				WSSessions.get().releaseSession(session);
				session = null;
				to = null;
				WorkerPool.done(this);
			}
			
		}
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		return count == ((ResponseWorker)obj).count;
	}
	
}